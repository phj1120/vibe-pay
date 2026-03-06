package com.api.app.aop

import com.api.app.common.security.SecurityUtils
import com.api.app.entity.SystemEntity
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class SystemColumnAspect(private val securityUtils: SecurityUtils) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Before("execution(* com.api.app.repository.rwdb..*.save*(..)) && args(entity,..)")
    fun setSystemColumns(joinPoint: JoinPoint, entity: Any) {
        if (entity !is SystemEntity) return
        val currentUser = getCurrentUser()
        if (entity.registId.isEmpty()) {
            entity.registId = currentUser
        }
        entity.modifyId = currentUser
    }

    private fun getCurrentUser(): String {
        return try {
            securityUtils.getCurrentUserMemberNo()
        } catch (e: Exception) {
            log.warn("사용자 정보 조회 실패, 비회원으로 설정합니다: {}", e.message)
            "999999999999999"
        }
    }
}
