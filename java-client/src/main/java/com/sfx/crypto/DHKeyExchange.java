package com.sfx.crypto;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Implementation of Diffie-Hellman key exchange for secure communication
 */
public class DHKeyExchange {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private KeyPair keyPair;
    private KeyAgreement keyAgreement;

    /**
     * Initialize Diffie-Hellman key exchange
     */
    public DHKeyExchange() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH", "BC");
            keyPairGenerator.initialize(2048);
            this.keyPair = keyPairGenerator.generateKeyPair();
            
            this.keyAgreement = KeyAgreement.getInstance("DH", "BC");
            this.keyAgreement.init(keyPair.getPrivate());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DH key exchange", e);
        }
    }

    /**
     * Get the public key in Base64 format
     */
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    /**
     * Compute the shared secret using the peer's public key
     * @param peerPublicKeyBase64 Peer's public key in Base64 format
     * @return Shared secret key in bytes
     */
    public byte[] computeSharedSecret(String peerPublicKeyBase64) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        byte[] peerPublicKeyBytes = Base64.getDecoder().decode(peerPublicKeyBase64);
        KeyFactory keyFactory = KeyFactory.getInstance("DH");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(peerPublicKeyBytes);
        PublicKey peerPublicKey = keyFactory.generatePublic(keySpec);
        
        KeyAgreement keyAgreement;
        keyAgreement = KeyAgreement.getInstance("DH");
        keyAgreement.init(keyPair.getPrivate());
        keyAgreement.doPhase(peerPublicKey, true);
        
        return keyAgreement.generateSecret();
    }

    /**
     * Derive an encryption key from the shared secret
     * @param sharedSecret Shared secret bytes from computeSharedSecret
     * @return Encryption key that can be used for symmetric encryption
     */
    public byte[] deriveEncryptionKey(byte[] sharedSecret) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(sharedSecret);
    }
}
