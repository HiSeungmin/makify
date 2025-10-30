package com.xladmt.makify.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.Entity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class SensitiveDataMasker {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Arrays.asList(
        "password",
        "token",
        "accessToken",
        "refreshToken",
        "authorization",
        "cardNumber",
        "cardPass",
        "cvv",
        "ssn",
        "phoneNumber",
        "email",
        "secret",
        "apiKey",
        "privateKey",
        "refreshtoken",
        "accesstoken"
    ));

    public String maskJsonString(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return jsonString;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            JsonNode maskedNode = maskJsonNode(jsonNode);
            return objectMapper.writeValueAsString(maskedNode);
        } catch (Exception e) {
            log.debug("JSON parsing failed: {}", e.getMessage());
            return maskStringValue(jsonString);
        }
    }

    public String maskObject(Object obj) {
        if (obj == null) {
            return "null";
        }

        try {
            // List인 경우 크기만 표시
            if (obj instanceof java.util.Collection) {
                java.util.Collection<?> collection = (java.util.Collection<?>) obj;
                return String.format("[Collection of %d items]", collection.size());
            }
            
            // Entity 객체인 경우 간단하게 표시
            if (isEntity(obj)) {
                return String.format("[%s]", obj.getClass().getSimpleName());
            }
            
            String jsonString = objectMapper.writeValueAsString(obj);
            return maskJsonString(jsonString);
        } catch (Exception e) {
            log.debug("Object serialization failed: {}", e.getMessage());
            return String.format("[%s]", obj.getClass().getSimpleName());
        }
    }
    
    /**
     * Entity 객체 여부 판단
     */
    private boolean isEntity(Object obj) {
        Class<?> clazz = obj.getClass();
        return clazz.isAnnotationPresent(Entity.class);
    }

    public String maskHeaders(String headerString) {
        if (headerString == null || headerString.isEmpty()) {
            return headerString;
        }

        String result = headerString;
        result = result.replaceAll(
            "(?i)(authorization\\s*:\\s*)(Bearer\\s+[^,\\}\\]]+)",
            "$1Bearer ***MASKED***"
        );
        result = result.replaceAll(
            "(?i)(token\\s*[:=]\\s*)([^,\\}\\]]+)",
            "$1***MASKED***"
        );

        return result;
    }

    public String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }

        String[] parts = email.split("@");
        if (parts.length != 2) {
            return email;
        }

        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return localPart + "@" + domain;
        }

        String maskedLocal = localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1);
        return maskedLocal + "@" + domain;
    }

    public String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return phoneNumber;
        }

        String digits = phoneNumber.replaceAll("[^0-9]", "");
        
        if (digits.length() < 8) {
            return phoneNumber;
        }

        String lastFour = digits.substring(digits.length() - 4);
        return "***-****-" + lastFour;
    }

    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return cardNumber;
        }

        String digits = cardNumber.replaceAll("[^0-9]", "");

        if (digits.length() < 8) {
            return cardNumber;
        }

        String firstFour = digits.substring(0, 4);
        String lastFour = digits.substring(digits.length() - 4);
        return firstFour + "-****-****-" + lastFour;
    }

    private String maskStringValue(String value) {
        if (value == null || value.length() < 4) {
            return value;
        }

        if (value.matches("^eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$")) {
            return "***JWT_TOKEN_MASKED***";
        }

        if (value.startsWith("Bearer ")) {
            return "Bearer ***MASKED***";
        }

        return value;
    }

    private JsonNode maskJsonNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();

                if (isSensitiveField(fieldName)) {
                    if (fieldValue.isTextual()) {
                        String value = fieldValue.asText();
                        String maskedValue = maskFieldValue(fieldName, value);
                        objectNode.put(fieldName, maskedValue);
                    } else {
                        objectNode.put(fieldName, "***MASKED***");
                    }
                } else if (fieldValue.isObject() || fieldValue.isArray()) {
                    objectNode.set(fieldName, maskJsonNode(fieldValue));
                }
            });
        } else if (node.isArray()) {
            node.forEach(this::maskJsonNode);
        }

        return node;
    }

    private boolean isSensitiveField(String fieldName) {
        return SENSITIVE_FIELDS.stream()
            .anyMatch(field -> field.equalsIgnoreCase(fieldName));
    }

    private String maskFieldValue(String fieldName, String value) {
        String lowerFieldName = fieldName.toLowerCase();

        if (lowerFieldName.contains("email")) {
            return maskEmail(value);
        } else if (lowerFieldName.contains("phone")) {
            return maskPhoneNumber(value);
        } else if (lowerFieldName.contains("card")) {
            return maskCardNumber(value);
        } else if (lowerFieldName.contains("token") || lowerFieldName.contains("password")) {
            return "***MASKED***";
        } else {
            return "***MASKED***";
        }
    }
}
