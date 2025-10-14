### 쿼리 컨벤션

MyBatis 매퍼 XML 파일의 쿼리는 다음 표준 스타일을 따릅니다.

- **쿼리 ID 주석:** 쿼리 시작 부분에 `/* Mapper.id */` 형식의 주석을 작성하여 식별을 용이하게 합니다.

- **키워드 및 컬럼명 대문자 작성 (매우 중요):**
    - **SQL 키워드(SELECT, FROM, WHERE 등)는 반드시 대문자로 작성**합니다.
    - **테이블명과 컬럼명도 대문자(SNAKE_CASE)로 작성**합니다. (예: `MEMBER_ID`, `CREATED_AT`)
    - MyBatis 파라미터(#{})는 카멜케이스로 작성합니다. (예: `#{memberId}`, `#{langCd}`)
    - **각 절(Clause)의 시작점은 세로로 완벽하게 정렬합니다.** 이는 쿼리의 전체 구조를 한눈에 파악하고 가독성을 극대화하는 핵심 규칙입니다.
    - `INNER JOIN`, `LEFT JOIN` 등 `JOIN` 절 또한 `FROM` 절과 동일한 레벨로 정렬합니다.
    - `AND`, `OR`와 같은 조건절 키워드도 `WHERE` 절의 시작점에 맞춰 세로로 정렬합니다.
    - **참고:** IntelliJ 자동 포맷터(`Cmd+Option+L` 또는 `Ctrl+Alt+L`)는 이 세로 정렬을 완벽하게 지원하지 못할 수 있습니다. **자동 포맷팅 후, 가독성을 위해 주요 키워드(FROM, WHERE, JOIN, AND 등)의 시작점을 수동으로 맞춰주는 작업이 필요합니다.**

- **들여쓰기:** 서브쿼리, `CASE` 문, `ON` 조건절 등은 상위 절보다 2~4 spaces 들여쓰기하여 계층 구조를 명확히 표현합니다.

- **컬럼 정렬 (매우 중요):** `SELECT` 절의 컬럼들은 **시작점을 맞춰 세로로 완벽하게 정렬**합니다. 첫 컬럼은 `SELECT` 키워드 바로 아래에 위치시키고, 이후 컬럼들은 콤마(`,`)를 앞에 붙여 정렬합니다.

- **주석:** 복잡한 비즈니스 로직이나 조건절에는 `/* 설명 */` 형식으로 주석을 추가하여 의도를 명확히 합니다.

- **동적 쿼리:** `<if>`, `<foreach>`, `<where>` 등 MyBatis 동적 쿼리 태그를 적극적으로 활용하여 유연한 쿼리를 작성합니다.

- **예시 (최종 정렬본):**

#### 기본 SELECT 예시
```xml
<select id="findByEmail" resultMap="memberResultMap">
    SELECT /* MemberMapper.findByEmail */
           MEMBER_ID
         , NAME
         , SHIPPING_ADDRESS
         , PHONE_NUMBER
         , EMAIL
         , CREATED_AT
      FROM MEMBER
     WHERE EMAIL = #{email}
</select>
```

#### INSERT 예시
```xml
<insert id="insert" useGeneratedKeys="true" keyProperty="memberId">
    INSERT INTO MEMBER ( /* MemberMapper.insert */
           NAME
         , SHIPPING_ADDRESS
         , PHONE_NUMBER
         , EMAIL
         , CREATED_AT
    ) 
    VALUES (
           #{name}
         , #{shippingAddress}
         , #{phoneNumber}
         , #{email}
         , #{createdAt}
    )
</insert>
```

#### UPDATE 예시
```xml
<update id="update">
    UPDATE /* MemberMapper.delete */ 
           MEMBER
       SET NAME = #{name}
         , SHIPPING_ADDRESS = #{shippingAddress}
         , PHONE_NUMBER = #{phoneNumber}
         , EMAIL = #{email}
     WHERE MEMBER_ID = #{memberId}
</update>
```

#### DELETE 예시
```xml
<delete id="delete">
    DELETE /* MemberMapper.delete */
      FROM MEMBER
     WHERE MEMBER_ID = #{memberId}
</delete>
```

#### JOIN 예시
```xml
<select id="findOrderWithMember" resultMap="orderResultMap">
    SELECT /* OrderMapper.findOrderWithMember */
           O.ORDER_ID
         , O.ORDER_DATE
         , O.TOTAL_AMOUNT
         , M.MEMBER_ID
         , M.NAME
         , M.EMAIL
      FROM ORDERS O
     INNER JOIN MEMBER M ON O.MEMBER_ID = M.MEMBER_ID
     WHERE O.ORDER_ID = #{orderId}
</select>
```

#### LEFT JOIN 예시
```xml
<select id="findMemberWithOrders" resultMap="memberOrderResultMap">
    SELECT /* MemberMapper.findMemberWithOrders */
           M.MEMBER_ID
         , M.NAME
         , M.EMAIL
         , O.ORDER_ID
         , O.ORDER_DATE
         , O.TOTAL_AMOUNT
      FROM MEMBER M
      LEFT JOIN ORDERS O ON M.MEMBER_ID = O.MEMBER_ID
     WHERE M.MEMBER_ID = #{memberId}
</select>
```

#### CASE WHEN 예시
```xml
<select id="findMemberWithStatus" resultMap="memberResultMap">
    SELECT /* MemberMapper.findMemberWithStatus */
           MEMBER_ID
         , NAME
         , EMAIL
         , CASE
               WHEN CREATED_AT >= NOW() - INTERVAL '30 days' THEN 'NEW'
               WHEN CREATED_AT >= NOW() - INTERVAL '365 days' THEN 'ACTIVE'
               ELSE 'INACTIVE'
           END AS MEMBER_STATUS
      FROM MEMBER
     WHERE MEMBER_ID = #{memberId}
</select>
```

#### 서브쿼리 예시
```xml
<select id="findMembersWithOrderCount" resultMap="memberStatsResultMap">
    SELECT /* MemberMapper.findMembersWithOrderCount */
           M.MEMBER_ID
         , M.NAME
         , M.EMAIL
         , (
               SELECT COUNT(*)
                 FROM ORDERS O
                WHERE O.MEMBER_ID = M.MEMBER_ID
           ) AS ORDER_COUNT
      FROM MEMBER M
     WHERE M.MEMBER_ID = #{memberId}
</select>
```

#### 동적 쿼리 (if, foreach) 예시
```xml
<select id="searchMembers" resultMap="memberResultMap">
    SELECT /* MemberMapper.searchMembers */
           MEMBER_ID
         , NAME
         , EMAIL
         , PHONE_NUMBER
      FROM MEMBER
     WHERE 1=1
       <if test="name != null and name != ''">
       AND NAME LIKE CONCAT('%', #{name}, '%')
       </if>
       <if test="email != null and email != ''">
       AND EMAIL = #{email}
       </if>
       <if test="memberIds != null and memberIds.size() > 0">
       AND MEMBER_ID IN
           <foreach collection="memberIds" item="memberId" separator="," open="(" close=")">
               #{memberId}
           </foreach>
       </if>
  ORDER BY CREATED_AT DESC
</select>
```

#### 복잡한 쿼리 예시 (JOIN + 서브쿼리 + CASE + 동적 쿼리)
```xml
<select id="getCouponInfoList" parameterType="CouponInfoRequest" resultType="CouponInfoResponse">
    SELECT /* CcPromBaseMapper.getCouponInfoList */
           CP.PROMO_NO
         , CP.PROMO_NM
         , CP.PROMO_DESC
         , CP.PROMO_GB_CD
         , CASE WHEN CP.APLY_PSB_MEDIA_CD = '03' THEN TRUE ELSE FALSE END AS APP_ONLY
      FROM (
               SELECT CPB.PROMO_NO
                    , ML.PROMO_NM
                    , ML.PROMO_DESC
                 FROM CC_PROM_BASE CPB
                INNER JOIN CC_PROMO_APLY_MEDIA_INFO CPSMI ON (CPB.PROMO_NO = CPSMI.PROMO_NO)
                 LEFT JOIN CC_PROM_BASE_ML ML ON CPB.PROMO_NO = ML.PROMO_NO AND ML.LANG_CD = #{langCd}
                WHERE CPB.PROMO_STAT_CD = '10'
                  AND NOW() BETWEEN CPB.PROMO_STR_DTM AND CPB.PROMO_END_DTM
                  AND CPB.PROMO_NO IN
                      <foreach collection="promoNoList" item="promoNo" separator="," open="(" close=")">
                          #{promoNo}
                      </foreach>
           ) CP
     WHERE CP.RANK = 1
</select>
```
