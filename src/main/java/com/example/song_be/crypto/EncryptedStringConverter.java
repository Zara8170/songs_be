package com.example.song_be.crypto;

import com.example.song_be.util.CryptoUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Converter( autoApply = false)
@RequiredArgsConstructor
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private CryptoUtil cryptoUtil;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            return cryptoUtil.encrypt(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("암호화 실패", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return cryptoUtil.decrypt(dbData);
        } catch (Exception e) {
            throw new IllegalStateException("복호화 실패", e);
        }
    }
}
