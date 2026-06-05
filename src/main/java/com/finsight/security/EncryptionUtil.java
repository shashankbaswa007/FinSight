package com.finsight.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for AES-256-GCM encryption/decryption of sensitive fields.
 * 
 * Uses AES/GCM/NoPadding (authenticated encryption with associated data):
 * - Provides both confidentiality and integrity/authentication
 * - Uses a random 12-byte IV prepended to the ciphertext
 * - 128-bit authentication tag appended by GCM automatically
 * 
 * Used alongside Jasypt for field-level encryption of:
 * - Transaction amounts
 * - Transaction descriptions
 * - Recurring transaction amounts
 * 
 * Thread-safe for concurrent use.
 */
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;  // 96 bits (NIST recommended)
    private static final int GCM_TAG_LENGTH = 128; // 128-bit authentication tag

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Encrypts a string using AES-256-GCM.
     * A random 12-byte IV is generated and prepended to the ciphertext.
     * 
     * Output format: Base64(IV || ciphertext || GCM-tag)
     * 
     * @param data The plaintext to encrypt
     * @param key  The encryption key (32 bytes for AES-256)
     * @return Base64-encoded IV + encrypted data + GCM tag
     * @throws Exception if encryption fails
     */
    public static String encrypt(String data, byte[] key) throws Exception {
        // Generate random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        SECURE_RANDOM.nextBytes(iv);

        SecretKey secretKey = new SecretKeySpec(key, 0, key.length, ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
        byte[] encryptedData = cipher.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        // Prepend IV to ciphertext: IV || ciphertext+tag
        byte[] combined = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Decrypts a Base64-encoded encrypted string using AES-256-GCM.
     * Expects the IV to be prepended to the ciphertext (as produced by encrypt()).
     * 
     * @param encryptedData Base64-encoded IV + encrypted data + GCM tag
     * @param key           The decryption key (32 bytes for AES-256)
     * @return The decrypted plaintext
     * @throws Exception if decryption fails or authentication tag verification fails
     */
    public static String decrypt(String encryptedData, byte[] key) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedData);

        // Extract IV from the beginning
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);

        // Extract ciphertext+tag (everything after IV)
        byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH];
        System.arraycopy(combined, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

        SecretKey secretKey = new SecretKeySpec(key, 0, key.length, ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
        byte[] decryptedData = cipher.doFinal(ciphertext);

        return new String(decryptedData, java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Generates a random AES-256 key.
     * 
     * @return A 256-bit SecretKey suitable for AES encryption
     * @throws Exception if key generation fails
     */
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE, new SecureRandom());
        return keyGenerator.generateKey();
    }

    /**
     * Converts a SecretKey to a Base64-encoded string for storage.
     * 
     * @param key The SecretKey to encode
     * @return Base64-encoded key
     */
    public static String keyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Converts a Base64-encoded key string back to a SecretKey.
     * 
     * @param encodedKey The Base64-encoded key
     * @return The SecretKey object
     */
    public static SecretKey stringToKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }

    /**
     * Checks if a string appears to be encrypted (starts with "ENC(").
     * Useful for migration logic to detect already-encrypted values.
     * 
     * @param value The string to check
     * @return true if the value starts with "ENC(", false otherwise
     */
    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith("ENC(");
    }
}

