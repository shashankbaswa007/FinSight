package com.finsight.config;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Jasypt field-level encryption.
 * 
 * Encrypts sensitive fields in the database:
 * - Transaction.amount, Transaction.description
 * - RecurringTransaction.amount
 * 
 * Uses AES-256 with a password-based key derived from JASYPT_ENCRYPTOR_PASSWORD env variable.
 * 
 * Usage:
 *   In database: amounts stored as encrypted strings (e.g., "ENC(xyz...)")
 *   In Java: @Encrypted annotation on fields auto-decrypts on load, encrypts on save
 */
@Configuration
@EnableEncryptableProperties
public class EncryptionConfiguration {

    @Value("${jasypt.encryptor.password:#{environment.getProperty('JASYPT_ENCRYPTOR_PASSWORD')}}")
    private String encryptorPassword;

    @Bean(name = "jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        
        config.setPassword(encryptorPassword != null ? encryptorPassword : "default-dev-password-change-in-prod");
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        
        encryptor.setConfig(config);
        return encryptor;
    }
}
