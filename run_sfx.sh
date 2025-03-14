#!/bin/bash

# Script to run the SecureFileXchange application

echo "Starting SecureFileXchange (SFX) application..."

# Base directory
BASE_DIR="/Users/sthwalonyoni/sfx"
PYTHON_SERVER_DIR="${BASE_DIR}/python-server"
JAVA_CLIENT_DIR="${BASE_DIR}/java-client"

# Check for dependencies
if ! command -v python3 &> /dev/null; then
    echo "Error: python3 is required but not installed."
    exit 1
fi

if ! command -v javac &> /dev/null; then
    echo "Warning: Java compiler not found. You may not be able to build the Java client."
fi

# Check if port 8000 is already in use
if lsof -i:8000 &> /dev/null; then
    echo "Warning: Port 8000 is already in use. Attempting to kill the process..."
    # Kill the process using port 8000
    PID=$(lsof -ti:8000)
    if [ ! -z "$PID" ]; then
        echo "Killing process $PID that is using port 8000"
        kill -9 $PID
        sleep 2
    fi
fi

# Function to start the Python server
start_server() {
    echo "Starting Python FastAPI server..."
    cd "${PYTHON_SERVER_DIR}"
    
    # Check if virtual environment exists, if not create it
    if [ ! -d "venv" ]; then
        echo "Creating Python virtual environment..."
        python3 -m venv venv
        source venv/bin/activate
        python -m pip install -r requirements.txt
    else
        source venv/bin/activate
    fi
    
    # Run the server
    python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload &
    SERVER_PID=$!
    echo "Server started with PID: $SERVER_PID"
    echo "Server URL: http://localhost:8000"
    echo "API documentation: http://localhost:8000/docs"
    
    # Wait for server to start
    echo "Waiting for server to start..."
    sleep 3
}

# Function to build and run the Java client
build_and_run_client() {
    if ! command -v mvn &> /dev/null; then
        echo "Error: Maven is required to build the Java client but not installed."
        return 1
    fi
    
    echo "Building Java client..."
    cd "${JAVA_CLIENT_DIR}"
    
    if [ ! -f "pom.xml" ]; then
        echo "Error: pom.xml not found in ${JAVA_CLIENT_DIR}"
        return 1
    fi
    
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        echo "Running Java client..."
        mvn javafx:run
    else
        echo "Failed to build Java client."
        return 1
    fi
}

# Start the server
start_server

# Check if server is running
if curl -s http://localhost:8000/health > /dev/null; then
    echo "Server is running successfully!"
    
    # Ask user if they want to run the client
    read -p "Do you want to build and run the Java client? (y/n): " run_client
    if [ "$run_client" = "y" ]; then
        build_and_run_client
    else
        echo "You can run the Java client later with:"
        echo "cd ${JAVA_CLIENT_DIR} && mvn javafx:run"
    fi
else
    echo "Error: Failed to start server or server is not responding."
fi

# Handle script termination
cleanup() {
    echo "Shutting down SFX application..."
    if [ ! -z "$SERVER_PID" ]; then
        echo "Stopping server (PID: $SERVER_PID)"
        kill $SERVER_PID 2>/dev/null
    fi
    exit 0
}

# Set up trap to catch SIGINT (Ctrl+C) and SIGTERM
trap cleanup SIGINT SIGTERM

# Keep script running
echo "Press Ctrl+C to stop the application"
while true; do
    sleep 1
done
