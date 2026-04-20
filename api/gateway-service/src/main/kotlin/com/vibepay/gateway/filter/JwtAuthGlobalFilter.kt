package com.vibepay.gateway.filter

import com.api.app.common.jwt.JwtTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtAuthGlobalFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : GlobalFilter, Ordered {

    private val log = LoggerFactory.getLogger(this::class.java)

    private val publicPaths = listOf(
        "/api/members/register",
        "/api/members/login",
        "/api/members/refresh",
        "/swagger-ui",
        "/v3/api-docs",
        "/api/pay/complete"
    )

    private val publicPrefixes = listOf("/api/goods")

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val path = exchange.request.path.value()

        if (isPublicPath(path)) {
            return chain.filter(exchange)
        }

        val token = extractToken(exchange) ?: return unauthorized(exchange)

        return try {
            if (jwtTokenProvider.isTokenExpired(token)) {
                return unauthorized(exchange, "EXPIRED_TOKEN")
            }
            val email = jwtTokenProvider.extractEmail(token)
            val mutated = exchange.mutate()
                .request { it.header("X-User-Email", email) }
                .build()
            chain.filter(mutated)
        } catch (e: Exception) {
            log.warn("JWT validation failed: {}", e.message)
            unauthorized(exchange)
        }
    }

    private fun isPublicPath(path: String): Boolean {
        if (publicPaths.any { path.startsWith(it) }) return true
        if (publicPrefixes.any { path.startsWith(it) }) return true
        return false
    }

    private fun extractToken(exchange: ServerWebExchange): String? {
        val auth = exchange.request.headers.getFirst("Authorization") ?: return null
        return if (auth.startsWith("Bearer ")) auth.substring(7) else null
    }

    private fun unauthorized(exchange: ServerWebExchange, code: String = "INVALID_TOKEN"): Mono<Void> {
        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        exchange.response.headers.add("Content-Type", "application/json;charset=UTF-8")
        val body = """{"code":"2001","message":"인증이 필요합니다"}"""
        val bytes = body.toByteArray()
        val buffer = exchange.response.bufferFactory().wrap(bytes)
        return exchange.response.writeWith(Mono.just(buffer))
    }

    override fun getOrder(): Int = -100
}
