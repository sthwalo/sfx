import requests
import os
import json

def test_health_endpoint():
    response = requests.get("http://localhost:8000/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok"}
    print("✓ Health endpoint test passed")

def test_key_exchange():
    # Initialize key exchange
    init_response = requests.post("http://localhost:8000/api/key-exchange/init")
    assert init_response.status_code == 201
    data = init_response.json()
    assert "session_id" in data
    assert "public_key" in data
    assert "parameters" in data
    
    print("✓ Key exchange initialization test passed")
    return data["session_id"], data["public_key"]

def test_file_operations():
    # Create a test file
    test_file_path = "test_upload.txt"
    with open(test_file_path, "w") as f:
        f.write("This is a test file for SFX")
    
    try:
        # Test file upload
        with open(test_file_path, "rb") as f:
            files = {"file": ("test_upload.txt", f)}
            data = {"user_id": "test_user"}
            response = requests.post("http://localhost:8000/api/files/upload", files=files, data=data)
        
        assert response.status_code == 200
        upload_data = response.json()
        assert "stored_filename" in upload_data
        stored_filename = upload_data["stored_filename"]
        
        print(f"✓ File upload test passed. Stored as: {stored_filename}")
        
        # Test file list
        list_response = requests.get("http://localhost:8000/api/files/list")
        assert list_response.status_code == 200
        list_data = list_response.json()
        assert "files" in list_data
        assert len(list_data["files"]) > 0
        
        print("✓ File list test passed")
        
        # Test file download
        download_response = requests.get(f"http://localhost:8000/api/files/download/{stored_filename}")
        assert download_response.status_code == 200
        assert len(download_response.content) > 0
        
        print("✓ File download test passed")
        
    finally:
        # Clean up the test file
        if os.path.exists(test_file_path):
            os.remove(test_file_path)

def main():
    print("Running API tests...")
    try:
        test_health_endpoint()
        session_id, public_key = test_key_exchange()
        test_file_operations()
        print("\nAll tests passed successfully!")
    except Exception as e:
        print(f"\nTest failed: {str(e)}")
        print("Make sure the server is running on http://localhost:8000")

if __name__ == "__main__":
    main()
