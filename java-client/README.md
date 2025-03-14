# SecureFileXchange (SFX) Java Client

This is the Java client component of the SecureFileXchange (SFX) application, designed to work with the Python FastAPI server.

## Overview

The Java client provides a user-friendly interface to interact with the SFX server, allowing users to:

- Securely connect to the server using Diffie-Hellman key exchange
- Upload files to the server
- Download files from the server
- View a list of available files

## Architecture

The Java client consists of the following components:

1. **API Client**: Handles HTTP communication with the FastAPI server
2. **Crypto Module**: Manages secure key exchange and encryption/decryption
3. **JavaFX UI**: Provides a user interface for interacting with the system

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- The Python server component running (see the python-server directory)

## Building and Running

### Building with Maven

```bash
cd /path/to/sfx/java-client
mvn clean package
```

This will create a JAR file in the `target` directory.

### Running the Client

You can run the client in two ways:

1. Using Maven:

```bash
mvn javafx:run
```

2. Using the JAR file:

```bash
java -jar target/sfx-client-1.0-SNAPSHOT.jar
```

### Running the Test Client

To run the test client that demonstrates API functionality:

```bash
mvn exec:java -Dexec.mainClass="com.sfx.ApiClientTest"
```

## How the Java Client and Python Server Work Together

1. **Connection and Key Exchange**:
   - The Java client initiates a connection to the Python server
   - The client requests a new Diffie-Hellman key exchange session
   - The server generates DH parameters and returns a session ID and public key
   - The client calculates a shared secret using the server's public key
   - Both sides now have the same shared secret that can be used for encryption

2. **File Upload**:
   - The client selects a file to upload
   - (Optionally) The file is encrypted using the shared key
   - The client sends the file to the server via a multipart form request
   - The server stores the file and returns a confirmation with the stored filename

3. **File Listing**:
   - The client requests a list of files from the server
   - The server queries its file storage and returns a list of available files
   - The client displays the files in the UI table

4. **File Download**:
   - The client selects a file to download
   - The client sends a download request to the server
   - The server retrieves the file and sends it to the client
   - (Optionally) The client decrypts the file if it was encrypted
   - The file is saved to the user's chosen location

## Security Features

- **Diffie-Hellman Key Exchange**: Securely establish a shared secret without transmitting it
- **AES Encryption**: Encrypt file contents for secure transmission
- **Audit Logging**: The server logs all operations for security auditing

## API Endpoints

The Java client interacts with the following Python server endpoints:

- `GET /health` - Check server health
- `POST /api/key-exchange/init` - Initiate key exchange
- `POST /api/key-exchange/complete` - Complete key exchange
- `POST /api/files/upload` - Upload a file
- `GET /api/files/download/{filename}` - Download a file
- `GET /api/files/list` - List available files

## Troubleshooting

1. **Connection Issues**:
   - Ensure the Python server is running (check with `http://localhost:8000/health`)
   - Check firewall settings if running on different machines

2. **Build Issues**:
   - Ensure you have Maven and Java 17+ installed
   - Check for dependency conflicts in the pom.xml

3. **Runtime Errors**:
   - Check the server logs for API errors
   - Enable debug logging for more details
cd /Users/sthwalonyoni/sfx
./run_sfx.sh