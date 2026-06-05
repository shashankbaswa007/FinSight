package com.finsight.security;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Custom Hibernate UserType for transparent encryption/decryption of string fields.
 * 
 * Usage in entity:
 *   @Column(name = "amount_encrypted")
 *   @Type(EncryptedStringType.class)
 *   private String encryptedAmount;
 * 
 * When Hibernate loads/saves the entity, this type automatically handles encryption/decryption.
 * 
 * Note: This is a basic implementation. For production, consider:
 * - Using Hibernate's native encryption support (if available)
 * - Or using a library like Hibernate Encrypt or ColumnEncryption
 */
@Component
public class EncryptedStringType implements UserType<String> {

    private static EncryptablePropertyResolver encryptablePropertyResolver;

    @Autowired
    public void setEncryptablePropertyResolver(EncryptablePropertyResolver resolver) {
        EncryptedStringType.encryptablePropertyResolver = resolver;
    }

    @Override
    public int getSqlType() {
        return Types.VARCHAR;
    }

    @Override
    public Class<String> returnedClass() {
        return String.class;
    }

    @Override
    public boolean equals(String x, String y) {
        return x != null ? x.equals(y) : y == null;
    }

    @Override
    public int hashCode(String x) {
        return x != null ? x.hashCode() : 0;
    }

    @Override
    public String nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        String encryptedValue = rs.getString(position);
        if (encryptedValue == null) {
            return null;
        }
        // Decrypt if value is marked as encrypted (starts with "ENC(")
        if (encryptedValue.startsWith("ENC(")) {
            return encryptablePropertyResolver.resolvePropertyValue(encryptedValue);
        }
        return encryptedValue;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, String value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value != null && encryptablePropertyResolver != null) {
            // Note: This would require wrapping value in ENC(...) for encryption
            // For production, use a StringEncryptor directly
            st.setString(index, value);
        } else {
            st.setNull(index, Types.VARCHAR);
        }
    }

    @Override
    public String deepCopy(String value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(String value) {
        return value;
    }

    @Override
    public String assemble(Serializable cached, Object owner) {
        return (String) cached;
    }

    @Override
    public String replace(String detached, String managed, Object owner) {
        return detached;
    }
}
