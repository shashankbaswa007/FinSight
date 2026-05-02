package com.finsight.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for AES-256 encryption/decryption of sensitive fields.
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
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final int KEY_SIZE = 256;

    /**
     * Encrypts a string using AES-256.
     * 
     * @param data The plaintext to encrypt
     * @param key  The encryption key (32 bytes for AES-256)
     * @return Base64-encoded encrypted data
     * @throws Exception if encryption fails
     */
    public static String encrypt(String data, byte[] key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, 0, key.length, ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * Decrypts a Base64-encoded encrypted string using AES-256.
     * 
     * @param encryptedData Base64-encoded encrypted data
     * @param key           The decryption key (32 bytes for AES-256)
     * @return The decrypted plaintext
     * @throws Exception if decryption fails
     */
    public static String decrypt(String encryptedData, byte[] key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, 0, key.length, ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedData = cipher.doFinal(decodedData);
        return new String(decryptedData);
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
