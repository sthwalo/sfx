from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

# Import routers
from app.routes import key_exchange, file_exchange

# Import database models
from app.audit_logs.database import engine
from app.audit_logs.models import Base as AuditBase

# Create database tables
AuditBase.metadata.create_all(bind=engine)

app = FastAPI(title="SecureFileXchange API", description="API for secure file exchange", version="0.1.0")

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, replace with specific origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(key_exchange.router)
app.include_router(file_exchange.router)

@app.get("/")
def read_root():
    return {"message": "Welcome to SecureFileXchange API"}

@app.get("/health")
def health_check():
    return {"status": "ok"}

if __name__ == "__main__":
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
