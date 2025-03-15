import os
import json
import pytest
from fastapi.testclient import TestClient

# Import the FastAPI app
from app.main import app

# Create a test client
client = TestClient(app)

# Mocked responses
MOCK_HEALTH_RESPONSE = {"status": "ok"}
MOCK_KEY_EXCHANGE_RESPONSE = {"session_id": "test_session_id", "public_key": "test_public_key", "parameters": "test_parameters"}
MOCK_UPLOAD_RESPONSE = {"stored_filename": "test_stored_filename", "filename": "test_upload.txt", "message": "File uploaded successfully"}
MOCK_LIST_RESPONSE = {"files": [{"filename": "test_stored_filename", "size_bytes": 100, "created_at": "2023-01-01T00:00:00"}]}

def test_health_endpoint():
    # Use TestClient instead of requests
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok"}
    print("✓ Health endpoint test passed")

def test_key_exchange():
    # Initialize key exchange using TestClient
    init_response = client.post("/api/key-exchange/init")
    assert init_response.status_code == 201
    data = init_response.json()
    assert "session_id" in data
    assert "public_key" in data
    assert "parameters" in data
    
    print("✓ Key exchange initialization test passed")
    return data["session_id"], data["public_key"]

def test_file_operations():
    # Create a test file
    test_file_path = os.path.join(os.path.dirname(__file__), "test_upload.txt")
    with open(test_file_path, "w") as f:
        f.write("This is a test file for SFX")
    
    try:
        # Test file upload using TestClient
        with open(test_file_path, "rb") as f:
            files = {"file": ("test_upload.txt", f)}
            data = {"user_id": "test_user"}
            response = client.post("/api/files/upload", files=files, data=data)
        
        assert response.status_code == 200
        upload_data = response.json()
        assert "stored_filename" in upload_data
        stored_filename = upload_data["stored_filename"]
        
        print(f"✓ File upload test passed. Stored as: {stored_filename}")
        
        # Test file list using TestClient
        list_response = client.get("/api/files/list")
        assert list_response.status_code == 200
        list_data = list_response.json()
        assert "files" in list_data
        assert len(list_data["files"]) > 0
        
        print("✓ File list test passed")
        
        # Test file download using TestClient
        download_response = client.get(f"/api/files/download/{stored_filename}")
        assert download_response.status_code == 200
        assert len(download_response.content) > 0
        
        print("✓ File download test passed")
        
    finally:
        # Clean up the test file
        if os.path.exists(test_file_path):
            os.remove(test_file_path)

# For CI compatibility - don't try to connect to localhost
@pytest.mark.parametrize("ci_test", [True])
def test_main(ci_test):
    print("Running API tests in CI mode...")
    try:
        test_health_endpoint()
        session_id, public_key = test_key_exchange()
        test_file_operations()
        print("\nAll tests passed successfully!")
    except Exception as e:
        pytest.fail(f"Test failed: {str(e)}")

# Keep this for local testing
if __name__ == "__main__":
    print("Running API tests in local mode...")
    try:
        test_health_endpoint()
        session_id, public_key = test_key_exchange()
        test_file_operations()
        print("\nLocal tests completed")
    except Exception as e:
        print(f"\nTest failed: {str(e)}")
        print("Make sure the server is running on http://localhost:8000")
