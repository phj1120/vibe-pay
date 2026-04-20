package com.vibepay.goods.aop

import com.api.app.entity.SystemEntity
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
@Component
class SystemColumnAspect {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Before("execution(* com.vibepay.goods.repository..*.save*(..)) && args(entity,..)")
    fun setSystemColumns(joinPoint: JoinPoint, entity: Any) {
        if (entity !is SystemEntity) return
        val userId = resolveUserId()
        if (entity.registId.isEmpty()) {
            entity.registId = userId
        }
        entity.modifyId = userId
    }

    private fun resolveUserId(): String {
        return try {
            val attrs = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            attrs?.request?.getHeader("X-User-Email") ?: "system"
        } catch (e: Exception) {
            log.warn("Failed to resolve user from header: {}", e.message)
            "system"
        }
    }
}
