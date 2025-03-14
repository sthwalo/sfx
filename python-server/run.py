import os
import sys
import uvicorn

def main():
    print("Starting SecureFileXchange API server")
    
    # Set Python path to include the project directory
    sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
    
    # Run the application using uvicorn
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )

if __name__ == "__main__":
    main()
