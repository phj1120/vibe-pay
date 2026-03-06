package com.api.app.common.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.function.Function
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.access-token-expiration}") private val accessTokenExpiration: Long,
    @Value("\${jwt.refresh-token-expiration}") private val refreshTokenExpiration: Long
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    fun generateAccessToken(email: String): String = createToken(email, accessTokenExpiration)

    fun generateRefreshToken(email: String): String = createToken(email, refreshTokenExpiration)

    private fun createToken(subject: String, expiration: Long): String {
        val now = Date()
        return Jwts.builder()
            .subject(subject)
            .issuedAt(now)
            .expiration(Date(now.time + expiration))
            .signWith(secretKey)
            .compact()
    }

    fun extractEmail(token: String): String = extractClaim(token, Claims::getSubject)

    fun extractExpiration(token: String): Date = extractClaim(token, Claims::getExpiration)

    fun <T> extractClaim(token: String, claimsResolver: Function<Claims, T>): T =
        claimsResolver.apply(extractAllClaims(token))

    private fun extractAllClaims(token: String): Claims {
        return try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).payload
        } catch (e: ExpiredJwtException) {
            log.debug("토큰이 만료되었지만 Claims 추출: {}", e.message)
            e.claims
        } catch (e: JwtException) {
            log.error("토큰 파싱 실패: {}", e.message)
            throw e
        }
    }

    fun isTokenExpired(token: String): Boolean {
        return try {
            extractExpiration(token).before(Date())
        } catch (e: ExpiredJwtException) {
            true
        } catch (e: JwtException) {
            true
        }
    }

    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        return try {
            extractEmail(token) == userDetails.username && !isTokenExpired(token)
        } catch (e: JwtException) {
            false
        }
    }

    fun validateToken(token: String, email: String): Boolean {
        return try {
            extractEmail(token) == email && !isTokenExpired(token)
        } catch (e: JwtException) {
            false
        }
    }
}
