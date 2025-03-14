from fastapi import APIRouter, HTTPException, status, Depends, UploadFile, File, Form
from fastapi.responses import FileResponse
from sqlalchemy.orm import Session
import os
import uuid
import shutil
from datetime import datetime

from app.audit_logs.database import get_db
from app.audit_logs.models import AuditLog

router = APIRouter(prefix="/api/files", tags=["file-exchange"])

# Create uploads directory if it doesn't exist
UPLOADS_DIR = "uploads"
os.makedirs(UPLOADS_DIR, exist_ok=True)

@router.post("/upload")
async def upload_file(
    file: UploadFile = File(...),
    user_id: str = Form(default="anonymous"),
    db: Session = Depends(get_db)
):
    # Generate a unique filename to prevent overwrites
    file_ext = os.path.splitext(file.filename)[1] if file.filename else ""
    unique_filename = f"{uuid.uuid4()}{file_ext}"
    file_path = os.path.join(UPLOADS_DIR, unique_filename)
    
    # Save the file
    try:
        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
    except Exception as e:
        # Log the error
        db.add(AuditLog(
            event_type="file_upload",
            user_id=user_id,
            success=False,
            details=f"Error: {str(e)}"
        ))
        db.commit()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to upload file: {str(e)}"
        )
    
    # Log successful upload
    db.add(AuditLog(
        event_type="file_upload",
        user_id=user_id,
        success=True,
        details=f"Uploaded file: {file.filename}, stored as: {unique_filename}"
    ))
    db.commit()
    
    return {
        "filename": file.filename,
        "stored_filename": unique_filename,
        "message": "File uploaded successfully"
    }

@router.get("/download/{filename}")
async def download_file(
    filename: str,
    user_id: str = "anonymous",
    db: Session = Depends(get_db)
):
    file_path = os.path.join(UPLOADS_DIR, filename)
    
    # Check if file exists
    if not os.path.exists(file_path):
        db.add(AuditLog(
            event_type="file_download",
            user_id=user_id,
            success=False,
            details=f"File not found: {filename}"
        ))
        db.commit()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="File not found"
        )
    
    # Log successful download
    db.add(AuditLog(
        event_type="file_download",
        user_id=user_id,
        success=True,
        details=f"Downloaded file: {filename}"
    ))
    db.commit()
    
    return FileResponse(
        path=file_path,
        filename=filename,
        media_type="application/octet-stream"
    )

@router.get("/list")
async def list_files(
    user_id: str = "anonymous",
    db: Session = Depends(get_db)
):
    # Log the list request
    db.add(AuditLog(
        event_type="file_list",
        user_id=user_id,
        success=True
    ))
    db.commit()
    
    # Get all files in the uploads directory
    try:
        files = []
        for filename in os.listdir(UPLOADS_DIR):
            file_path = os.path.join(UPLOADS_DIR, filename)
            if os.path.isfile(file_path):
                files.append({
                    "filename": filename,
                    "size_bytes": os.path.getsize(file_path),
                    "created_at": datetime.fromtimestamp(os.path.getctime(file_path)).isoformat()
                })
        return {"files": files}
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to list files: {str(e)}"
        )
