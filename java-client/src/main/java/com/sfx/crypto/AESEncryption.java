package com.sfx.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.util.Base64;

/**
 * AES encryption utility for secure file content encryption/decryption
 */
public class AESEncryption {
    // Static initialization of security provider
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * Encrypt data with AES-GCM
     * @param plaintext Data to encrypt
     * @param key Encryption key from Diffie-Hellman key exchange
     * @return Base64 encoded encrypted data with IV prepended
     */
    public static String encrypt(byte[] plaintext, byte[] key) throws Exception {
        // Generate random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        new java.security.SecureRandom().nextBytes(iv);

        // Initialize cipher for encryption
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        
        Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        
        // Encrypt
        byte[] ciphertext = cipher.doFinal(plaintext);
        
        // Prepend IV to ciphertext
        byte[] encryptedData = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, encryptedData, 0, iv.length);
        System.arraycopy(ciphertext, 0, encryptedData, iv.length, ciphertext.length);
        
        // Return Base64 encoded result
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * Decrypt AES-GCM encrypted data
     * @param encryptedBase64 Base64 encoded encrypted data with IV prepended
     * @param key Decryption key from Diffie-Hellman key exchange
     * @return Decrypted data
     */
    public static byte[] decrypt(String encryptedBase64, byte[] key) throws Exception {
        // Decode Base64
        byte[] encryptedData = Base64.getDecoder().decode(encryptedBase64);
        
        // Extract IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] ciphertext = new byte[encryptedData.length - GCM_IV_LENGTH];
        
        System.arraycopy(encryptedData, 0, iv, 0, iv.length);
        System.arraycopy(encryptedData, iv.length, ciphertext, 0, ciphertext.length);
        
        // Initialize cipher for decryption
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        
        Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        
        // Decrypt and return
        return cipher.doFinal(ciphertext);
    }
}
