package com.vibe.pay.common.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 해싱 유틸리티
 *
 * SHA-256, SHA-512 등의 해시 함수를 사용한 서명 생성을 담당합니다.
 * PG사 API 연동 시 서명(signature) 생성에 사용됩니다.
 *
 * @author system
 * @version 1.0
 * @since 2025-10-17
 */
@Slf4j
public class HashUtils {

    /**
     * SHA-256 해싱
     *
     * @param plainText 원본 문자열
     * @return 해시값 (16진수 문자열)
     */
    public static String sha256(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not found", e);
            throw new RuntimeException("SHA-256 hashing failed", e);
        }
    }

    /**
     * SHA-512 해싱
     *
     * @param plainText 원본 문자열
     * @return 해시값 (16진수 문자열)
     */
    public static String sha512(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-512 algorithm not found", e);
            throw new RuntimeException("SHA-512 hashing failed", e);
        }
    }

    /**
     * HMAC-SHA256 해싱
     *
     * @param plainText 원본 문자열
     * @param key 비밀키
     * @return 해시값 (16진수 문자열)
     */
    public static String hmacSha256(String plainText, String key) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec =
                    new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            log.error("HMAC-SHA256 hashing failed", e);
            throw new RuntimeException("HMAC-SHA256 hashing failed", e);
        }
    }

    /**
     * 바이트 배열을 16진수 문자열로 변환
     *
     * @param bytes 바이트 배열
     * @return 16진수 문자열
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
