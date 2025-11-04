# 시스템 컬럼 관리 가이드

> 모든 테이블에 공통으로 존재하는 시스템 컬럼(등록자, 등록일시, 수정자, 수정일시)의 관리 방법을 설명합니다.

## 목차

1. [개요](#1-개요)
2. [시스템 컬럼 구조](#2-시스템-컬럼-구조)
3. [Entity 작성 방법](#3-entity-작성-방법)
4. [AOP 동작 원리](#4-aop-동작-원리)
5. [Mapper XML 작성](#5-mapper-xml-작성)
6. [주의사항](#6-주의사항)

---

## 1. 개요

### 1.1 시스템 컬럼이란?

모든 데이터베이스 테이블에 공통으로 포함되어야 하는 다음 4개의 컬럼을 의미합니다:

- `REGIST_ID`: 등록자
- `REGIST_DATE_TIME`: 등록일시
- `MODIFY_ID`: 수정자
- `MODIFY_DATE_TIME`: 수정일시

### 1.2 자동 관리

시스템 컬럼은 **AOP(Aspect-Oriented Programming)**를 통해 자동으로 설정됩니다.
개발자가 직접 값을 설정할 필요가 없으며, INSERT/UPDATE 시 자동으로 처리됩니다.

---

## 2. 시스템 컬럼 구조

### 2.1 데이터베이스 스키마

모든 테이블에 다음 4개 컬럼이 포함되어야 합니다:

```sql
CREATE TABLE EXAMPLE_TABLE (
    -- 비즈니스 컬럼들...
    EXAMPLE_NO      VARCHAR(15)     NOT NULL,
    EXAMPLE_NAME    VARCHAR(50)     NOT NULL,

    -- 시스템 컬럼 (필수)
    REGIST_ID           VARCHAR(15)     NOT NULL,   -- 등록자
    REGIST_DATE_TIME    TIMESTAMP       NOT NULL DEFAULT NOW(),   -- 등록일시
    MODIFY_ID           VARCHAR(15)     NOT NULL,   -- 수정자
    MODIFY_DATE_TIME    TIMESTAMP       NOT NULL DEFAULT NOW()    -- 수정일시
);
```

### 2.2 DDL 규칙

- 컬럼명: 대문자 SNAKE_CASE (REGIST_ID, REGIST_DATE_TIME 등)
- 데이터 타입:
  - ID 컬럼: `VARCHAR(15)`
  - 일시 컬럼: `TIMESTAMP`
- NOT NULL 제약조건 필수
- DEFAULT 값: `NOW()`

---

## 3. Entity 작성 방법

### 3.1 SystemEntity 상속

**모든 Entity 클래스는 `SystemEntity`를 상속받아야 합니다.**

```java
@Alias("MemberBase")
@Getter
@Setter
public class MemberBase extends SystemEntity {
    private static final long serialVersionUID = 9012345678901234567L;

    @Schema(description = "회원번호")
    private String memberNo;

    @Schema(description = "회원명")
    private String memberName;

    // 시스템 컬럼은 SystemEntity에서 상속받으므로 선언하지 않음
}
```

### 3.2 SystemEntity 구조

```java
@Getter
@Setter
public abstract class SystemEntity implements Serializable {
    private static final long serialVersionUID = 1000000000000000001L;

    @Schema(description = "등록자")
    private String registId;

    @Schema(description = "등록일시")
    private LocalDateTime registDateTime;

    @Schema(description = "수정자")
    private String modifyId;

    @Schema(description = "수정일시")
    private LocalDateTime modifyDateTime;
}
```

### 3.3 Entity 작성 체크리스트

- [ ] `extends SystemEntity` 상속
- [ ] 시스템 컬럼을 Entity에 별도로 선언하지 않음
- [ ] `@Alias` 어노테이션 추가
- [ ] `serialVersionUID` 선언

---

## 4. AOP 동작 원리

### 4.1 SystemColumnAspect

`SystemColumnAspect` 클래스가 모든 Mapper의 INSERT/UPDATE 메소드를 가로채서 자동으로 시스템 컬럼을 설정합니다.

```java
@Aspect
@Component
@Slf4j
public class SystemColumnAspect {

    /**
     * INSERT 시: 모든 시스템 컬럼 세팅
     */
    @Before("execution(* com.api.app.repository..*TrxMapper.insert*(..)) && args(entity,..)")
    public void setInsertSystemColumns(JoinPoint joinPoint, SystemEntity entity) {
        String currentUser = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        entity.setRegistId(currentUser);
        entity.setRegistDateTime(now);
        entity.setModifyId(currentUser);
        entity.setModifyDateTime(now);
    }

    /**
     * UPDATE 시: 수정 관련 시스템 컬럼만 세팅
     */
    @Before("execution(* com.api.app.repository..*TrxMapper.update*(..)) && args(entity,..)")
    public void setUpdateSystemColumns(JoinPoint joinPoint, SystemEntity entity) {
        String currentUser = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        entity.setModifyId(currentUser);
        entity.setModifyDateTime(now);
    }
}
```

### 4.2 동작 흐름

#### INSERT 시

1. Service에서 `categoryTrxMapper.insertCategory(category)` 호출
2. AOP가 메소드 실행 전 가로챔 (Before Advice)
3. 현재 로그인한 사용자 정보를 세션에서 조회
4. `registId`, `registDateTime`, `modifyId`, `modifyDateTime` 자동 설정
5. Mapper 메소드 실행 → DB INSERT

#### UPDATE 시

1. Service에서 `categoryTrxMapper.updateCategory(category)` 호출
2. AOP가 메소드 실행 전 가로챔 (Before Advice)
3. 현재 로그인한 사용자 정보를 세션에서 조회
4. `modifyId`, `modifyDateTime`만 자동 설정 (registId, registDateTime은 유지)
5. Mapper 메소드 실행 → DB UPDATE

### 4.3 사용자 정보 조회

AOP는 다음 우선순위로 사용자 정보를 조회합니다:

1. **HttpSession의 `memberNo` 속성** (로그인한 사용자)
2. **"999999999999999"** (비회원 또는 세션 정보가 없을 경우 기본값)

```java
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
```

**비회원 ID**: `999999999999999`는 비회원 사용자를 나타내는 특수 ID입니다.

---

## 5. Mapper XML 작성

### 5.1 INSERT 쿼리

시스템 컬럼을 모두 포함해야 합니다:

```xml
<insert id="insertMemberBase" parameterType="MemberBase">
    /* MemberBaseTrxMapper.insertMemberBase */
    INSERT INTO MEMBER_BASE (
           MEMBER_NO
         , MEMBER_NAME
         , PHONE
         , EMAIL
         , PASSWORD
         , MEMBER_STATUS_CODE
         , REGIST_ID
         , REGIST_DATE_TIME
         , MODIFY_ID
         , MODIFY_DATE_TIME
    )
    VALUES (
           #{memberNo}
         , #{memberName}
         , #{phone}
         , #{email}
         , #{password}
         , #{memberStatusCode}
         , #{registId}
         , #{registDateTime}
         , #{modifyId}
         , #{modifyDateTime}
    )
</insert>
```

### 5.2 UPDATE 쿼리

수정 관련 시스템 컬럼만 포함합니다:

```xml
<update id="updateMemberBase" parameterType="MemberBase">
    /* MemberBaseTrxMapper.updateMemberBase */
    UPDATE MEMBER_BASE
       SET MEMBER_NAME = #{memberName}
         , PHONE = #{phone}
         , EMAIL = #{email}
         , MEMBER_STATUS_CODE = #{memberStatusCode}
         , MODIFY_ID = #{modifyId}
         , MODIFY_DATE_TIME = #{modifyDateTime}
     WHERE MEMBER_NO = #{memberNo}
</update>
```

### 5.3 SELECT 쿼리

조회 시에도 시스템 컬럼을 포함하는 것을 권장합니다:

```xml
<select id="selectMemberBase" parameterType="string" resultType="MemberBase">
    /* MemberBaseMapper.selectMemberBase */
    SELECT /* MemberBaseMapper.selectMemberBase */
           MEMBER_NO
         , MEMBER_NAME
         , PHONE
         , EMAIL
         , MEMBER_STATUS_CODE
         , REGIST_ID
         , REGIST_DATE_TIME
         , MODIFY_ID
         , MODIFY_DATE_TIME
      FROM MEMBER_BASE
     WHERE MEMBER_NO = #{memberNo}
</select>
```

---

## 6. 주의사항

### 6.1 필수 규칙

✅ **반드시 지켜야 할 사항**:

- [ ] 모든 테이블에 시스템 컬럼 4개 추가
- [ ] 모든 Entity는 `SystemEntity` 상속
- [ ] INSERT/UPDATE 쿼리에 시스템 컬럼 포함
- [ ] Mapper 인터페이스 명명 규칙 준수 (`*TrxMapper`)

❌ **하지 말아야 할 사항**:

- Service나 Controller에서 시스템 컬럼 값을 직접 설정하지 마세요
- SystemEntity를 상속받지 않은 Entity 사용하지 마세요
- 시스템 컬럼을 비즈니스 로직에서 조작하지 마세요

### 6.2 Mapper 명명 규칙

AOP가 올바르게 동작하려면 Mapper 인터페이스 명명 규칙을 지켜야 합니다:

```java
// ✅ 올바른 예시
public interface MemberBaseTrxMapper {
    int insertMemberBase(MemberBase memberBase);
    int updateMemberBase(MemberBase memberBase);
}

// ❌ 잘못된 예시
public interface MemberBaseMapper {  // TrxMapper가 아님
    int insertMemberBase(MemberBase memberBase);
}
```

### 6.3 트랜잭션 관리

INSERT/UPDATE는 반드시 `@Transactional` 어노테이션과 함께 사용해야 합니다:

```java
@Override
@Transactional(propagation = Propagation.REQUIRED,
               readOnly = false,
               value = "displayRwdbTxManager")
public void save(MemberBase memberBase) {
    memberBaseTrxMapper.insertMemberBase(memberBase);
}
```

### 6.4 세션 관리

로그인 시 세션에 `memberNo`를 저장해야 AOP가 사용자 정보를 올바르게 조회할 수 있습니다:

```java
// 로그인 성공 시
session.setAttribute("memberNo", member.getMemberNo());
```

---

## 체크리스트

### ✅ 데이터베이스

- [ ] 모든 테이블에 시스템 컬럼 4개 추가
- [ ] NOT NULL 제약조건 설정
- [ ] DEFAULT NOW() 설정

### ✅ Entity

- [ ] `extends SystemEntity` 상속
- [ ] 시스템 컬럼을 별도로 선언하지 않음
- [ ] `@Alias` 어노테이션 추가

### ✅ Mapper XML

- [ ] INSERT 쿼리에 시스템 컬럼 4개 모두 포함
- [ ] UPDATE 쿼리에 MODIFY_ID, MODIFY_DATE_TIME 포함
- [ ] SELECT 쿼리에 시스템 컬럼 포함 (권장)

### ✅ Mapper 인터페이스

- [ ] CUD 작업용 Mapper는 `*TrxMapper` 명명 규칙 준수
- [ ] 메소드명은 `insert*`, `update*`, `delete*` 규칙 준수

### ✅ Service

- [ ] INSERT/UPDATE는 `@Transactional` 어노테이션 사용
- [ ] 시스템 컬럼 값을 직접 설정하지 않음

---

**최종 수정일**: 2025-10-28
**버전**: 1.0
**작성자**: system
