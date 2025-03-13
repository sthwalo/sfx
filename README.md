# sfx
SecureFileXchange (SFX) application.

sfx/
├── java-client/               # Java 17+ (Maven)
│   ├── src/
│   │   ├── main/java/com/sfx/
│   │   │   ├── crypto/       # AES/RSA implementations
│   │   │   ├── auth/         # SRP6/TOTP
│   │   │   └── ui/          # JavaFX GUI
│   │   └── test/             # JUnit 5 tests
├── python-server/            # Python 3.10+
│   ├── app/
│   │   ├── dh_key_exchange/  # Diffie-Hellman
│   │   ├── audit_logs/       # SQLAlchemy models
│   │   └── routes/           # FastAPI endpoints
│   └── tests/                # pytest
├── docker/
│   ├── java-client.Dockerfile
│   └── python-server.Dockerfile
└── .github/workflows/         # CI/CD pipelines