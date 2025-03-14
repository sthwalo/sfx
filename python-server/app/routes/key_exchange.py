from fastapi import APIRouter, HTTPException, status, Response
from fastapi.responses import JSONResponse
from pydantic import BaseModel
import base64
import os

from app.dh_key_exchange.dh import DHKeyExchange, create_dh_exchange

router = APIRouter(prefix="/api/key-exchange", tags=["key-exchange"])

# Store active key exchanges (in a real app, this would be in a database)
active_exchanges = {}

class PublicKeyResponse(BaseModel):
    session_id: str
    public_key: str
    parameters: str

class PeerPublicKeyRequest(BaseModel):
    session_id: str
    public_key: str

@router.post("/init", response_model=PublicKeyResponse, status_code=status.HTTP_201_CREATED)
def initialize_key_exchange():
    # Create a new DH key exchange
    dh_exchange = create_dh_exchange()
    
    # Generate a unique session ID
    session_id = base64.urlsafe_b64encode(os.urandom(16)).decode('utf-8')
    
    # Store the exchange in active exchanges
    active_exchanges[session_id] = dh_exchange
    
    # Return the public key and parameters
    return {
        "session_id": session_id,
        "public_key": base64.b64encode(dh_exchange.get_public_key_bytes()).decode('utf-8'),
        "parameters": base64.b64encode(dh_exchange.get_parameters_bytes()).decode('utf-8')
    }

@router.post("/complete")
def complete_key_exchange(request: PeerPublicKeyRequest):
    # Check if session exists
    if request.session_id not in active_exchanges:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Session not found"
        )
    
    # Get the DH exchange
    dh_exchange = active_exchanges[request.session_id]
    
    try:
        # Decode the peer's public key
        peer_public_key_bytes = base64.b64decode(request.public_key)
        
        # Compute the shared key
        shared_key = dh_exchange.compute_shared_key(peer_public_key_bytes)
        
        # In a real application, we'd store this key securely
        # For now, we'll just confirm success
        
        # Clean up (remove the session)
        del active_exchanges[request.session_id]
        
        return {"status": "success", "message": "Key exchange completed successfully"}
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Failed to complete key exchange: {str(e)}"
        )
