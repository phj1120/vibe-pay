#### SELECT 예시

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
    UPDATE MEMBER /* MemberMapper.update */
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
