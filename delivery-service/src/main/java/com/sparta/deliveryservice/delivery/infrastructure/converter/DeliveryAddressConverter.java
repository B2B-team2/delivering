package com.sparta.deliveryservice.delivery.infrastructure.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.deliveryservice.delivery.domain.core.DeliveryAddress;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DeliveryAddressConverter implements AttributeConverter<DeliveryAddress, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(DeliveryAddress attribute) {
        try {
            return attribute == null ? null : objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("주소 객체를 JSON 문자열로 변환하는데 실패했습니다.", e);
        }
    }

    @Override
    public DeliveryAddress convertToEntityAttribute(String dbData) {
        try {
            return dbData == null || dbData.isBlank() ? null : objectMapper.readValue(dbData, DeliveryAddress.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 문자열을 주소 객체로 역직렬화하는데 실패했습니다.", e);
        }
    }
}