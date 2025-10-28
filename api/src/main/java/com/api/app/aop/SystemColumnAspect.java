package com.api.app.aop;

import com.api.app.entity.SystemEntity;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;

/**
 * SystemEntity의 공통 컬럼(등록자, 등록일시, 수정자, 수정일시)을 자동으로 세팅하는 AOP
 *
 * - INSERT 시: registId, registDateTime, modifyId, modifyDateTime 모두 세팅
 * - UPDATE 시: modifyId, modifyDateTime만 세팅
 *
 * @author system
 * @version 1.0
 * @since 2025-10-28
 */
@Aspect
@Component
@Slf4j
public class SystemColumnAspect {

    /**
     * INSERT 메소드 실행 전 시스템 컬럼 세팅
     * registId, registDateTime, modifyId, modifyDateTime 모두 세팅
     */
    @Before("execution(* com.api.app.repository..*TrxMapper.insert*(..)) && args(entity,..)")
    public void setInsertSystemColumns(JoinPoint joinPoint, SystemEntity entity) {
        String currentUser = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        log.debug("INSERT 시스템 컬럼 세팅 - User: {}, Entity: {}", currentUser, entity.getClass().getSimpleName());

        entity.setRegistId(currentUser);
        entity.setRegistDateTime(now);
        entity.setModifyId(currentUser);
        entity.setModifyDateTime(now);
    }

    /**
     * UPDATE 메소드 실행 전 시스템 컬럼 세팅
     * modifyId, modifyDateTime만 세팅
     */
    @Before("execution(* com.api.app.repository..*TrxMapper.update*(..)) && args(entity,..)")
    public void setUpdateSystemColumns(JoinPoint joinPoint, SystemEntity entity) {
        String currentUser = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        log.debug("UPDATE 시스템 컬럼 세팅 - User: {}, Entity: {}", currentUser, entity.getClass().getSimpleName());

        entity.setModifyId(currentUser);
        entity.setModifyDateTime(now);
    }

    /**
     * 현재 로그인한 사용자 정보를 가져옵니다.
     * 세션에서 memberNo를 가져오며, 없을 경우 비회원 ID(999999999999999)를 반환합니다.
     *
     * @return 현재 사용자 ID
     */
    private String getCurrentUser() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpSession session = attributes.getRequest().getSession(false);
                if (session != null) {
                    Object memberNo = session.getAttribute("memberNo");
                    if (memberNo != null) {
                        return memberNo.toString();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("사용자 정보 조회 실패, 비회원으로 설정합니다", e);
        }

        // 비회원 또는 세션 정보가 없을 경우 기본값
        return "999999999999999";
    }
}
