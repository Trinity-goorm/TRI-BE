package com.trinity.ctc.global.util.formatter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinity.ctc.global.exception.error_code.JsonParseErrorCode;
import com.trinity.ctc.global.exception.CustomException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * JSON 데이터 타입(DB)과 Map 자료형을 서로 변환해주는 컨버터
 * JSON 데이터를 문자열로만 전달하는 것이 아니라 내부의 값을 사용해야 할 경우
 *
 * 사용 예시. NotificationHistory Entity의 message column
 */
@RequiredArgsConstructor
@Converter
@Slf4j
public class JsonUtil implements AttributeConverter<Map<String, Object>, String> {
    private final ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (CollectionUtils.isEmpty(attribute)) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new CustomException(JsonParseErrorCode.COLUMN_CONVERT_FAILED);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (!StringUtils.hasText(dbData)) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new CustomException(JsonParseErrorCode.ENTITY_CONVERT_FAILED);
        }
    }
}

