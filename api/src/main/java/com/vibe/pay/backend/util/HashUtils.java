package com.vibe.pay.backend.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 결제 서명 생성 유틸리티
 *
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
public class HashUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * HMAC-SHA256 서명 생성
     *
     * @param data 서명할 데이터
     * @param key  서명 키
     * @return Base64 인코딩된 서명
     */
    public static String generateSignature(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    /**
     * SHA-256 해시 생성 (Hex 인코딩)
     *
     * @param data 해시할 데이터
     * @return Hex 인코딩된 해시
     */
    public static String sha256Hex(String data) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate SHA-256 hash", e);
        }
    }
}
