package com.example.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {
    private static final String HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private final SecurityProperties properties;
    private final ObjectMapper objectMapper;

    public JwtService(SecurityProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String create(Long userId, String tenantId, String username) {
        long now = Instant.now().getEpochSecond();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", String.valueOf(userId));
        payload.put("tenant", tenantId);
        payload.put("username", username);
        payload.put("iat", now);
        payload.put("exp", now + properties.getTokenTtlSeconds());
        try {
            String encodedHeader = encode(HEADER.getBytes(StandardCharsets.UTF_8));
            String encodedPayload = encode(objectMapper.writeValueAsBytes(payload));
            String content = encodedHeader + "." + encodedPayload;
            return content + "." + encode(sign(content));
        } catch (Exception exception) {
            throw new IllegalStateException("cannot create access token", exception);
        }
    }

    public Claims verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException("invalid token");
            String content = parts[0] + "." + parts[1];
            if (!MessageDigest.isEqual(sign(content), Base64.getUrlDecoder().decode(parts[2]))) {
                throw new IllegalArgumentException("invalid token signature");
            }
            Map<String, Object> payload = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(parts[1]), new TypeReference<Map<String, Object>>() { });
            long expiresAt = ((Number) payload.get("exp")).longValue();
            if (expiresAt <= Instant.now().getEpochSecond()) throw new IllegalArgumentException("token expired");
            return new Claims(Long.valueOf(String.valueOf(payload.get("sub"))),
                    String.valueOf(payload.get("tenant")), String.valueOf(payload.get("username")), expiresAt);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("invalid token", exception);
        }
    }

    private byte[] sign(String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(properties.getJwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    public static final class Claims {
        private final Long userId;
        private final String tenantId;
        private final String username;
        private final long expiresAt;

        public Claims(Long userId, String tenantId, String username, long expiresAt) {
            this.userId = userId;
            this.tenantId = tenantId;
            this.username = username;
            this.expiresAt = expiresAt;
        }

        public Long getUserId() { return userId; }
        public String getTenantId() { return tenantId; }
        public String getUsername() { return username; }
        public long getExpiresAt() { return expiresAt; }
    }
}
