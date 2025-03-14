import os
import secrets
from cryptography.hazmat.primitives.asymmetric import dh
from cryptography.hazmat.primitives import hashes, serialization

class DHKeyExchange:
    def __init__(self):
        # Generate parameters
        self.parameters = dh.generate_parameters(generator=2, key_size=2048)
        # Generate private key
        self.private_key = self.parameters.generate_private_key()
        # Get public key
        self.public_key = self.private_key.public_key()
    
    def get_public_key_bytes(self):
        # Get public key in bytes format for transmission
        return self.public_key.public_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PublicFormat.SubjectPublicKeyInfo
        )
    
    def get_parameters_bytes(self):
        # Get parameters in bytes format for transmission
        return self.parameters.parameter_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.ParameterFormat.PKCS3
        )
    
    def compute_shared_key(self, peer_public_key_bytes):
        # Deserialize peer's public key
        peer_public_key = serialization.load_pem_public_key(peer_public_key_bytes)
        # Compute shared key
        shared_key = self.private_key.exchange(peer_public_key)
        # Derive a key using HKDF
        return shared_key

# Example usage
def create_dh_exchange():
    return DHKeyExchange()
