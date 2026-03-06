package com.api.app.repository.rodb.order

import com.api.app.entity.OrderBase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface OrderCompleteHeaderProjection {
    fun getOrderNo(): String?
    fun getMemberNo(): String?
    fun getOrderAcceptDtm(): LocalDateTime?
    fun getTotalAmount(): Long?
}

interface OrderListFlatProjection {
    fun getOrderNo(): String?
    fun getOrderAcceptDtm(): LocalDateTime?
    fun getTotalAmount(): Long?
    fun getOrderSequence(): Long?
    fun getOrderProcessSequence(): Long?
    fun getGoodsNo(): String?
    fun getItemNo(): String?
    fun getGoodsName(): String?
    fun getItemName(): String?
    fun getSalePrice(): Long?
    fun getQuantity(): Long?
    fun getOrderStatusCode(): String?
    fun getOrderStatusName(): String?
    fun getOrderTypeCode(): String?
    fun getOrderTypeName(): String?
    fun getCancelable(): Boolean?
    fun getCancelableAmount(): Long?
}

interface CancelableOrderItemProjection {
    fun getOrderSequence(): Long?
    fun getOrderProcessSequence(): Long?
    fun getGoodsNo(): String?
    fun getItemNo(): String?
    fun getGoodsName(): String?
    fun getItemName(): String?
    fun getSalePrice(): Long?
    fun getQuantity(): Long?
    fun getSubtotal(): Long?
}

interface RefundDetailProjection {
    fun getPayWayCode(): String?
    fun getPayWayName(): String?
    fun getRefundAmount(): Long?
    fun getPgTypeCode(): String?
    fun getPgTypeName(): String?
}

interface OrderBaseRepository : JpaRepository<OrderBase, String> {

    fun findByMemberNo(memberNo: String): List<OrderBase>

    @Query(
        value = """
        SELECT OB.ORDER_NO AS orderNo
             , OB.MEMBER_NO AS memberNo
             , MIN(OD.ORDER_ACCEPT_DTM) AS orderAcceptDtm
             , SUM(OG.SALE_PRICE * OD.QUANTITY) AS totalAmount
          FROM ORDER_BASE OB
         INNER JOIN ORDER_DETAIL OD ON OB.ORDER_NO = OD.ORDER_NO
         INNER JOIN ORDER_GOODS OG ON OB.ORDER_NO = OG.ORDER_NO
           AND OD.GOODS_NO = OG.GOODS_NO AND OD.ITEM_NO = OG.ITEM_NO
         WHERE OB.ORDER_NO = :orderNo AND OB.MEMBER_NO = :memberNo
           AND OD.ORDER_TYPE_CODE = '001' AND OD.ORDER_PROCESS_SEQUENCE = 1
         GROUP BY OB.ORDER_NO, OB.MEMBER_NO
        """,
        nativeQuery = true
    )
    fun selectOrderCompleteByOrderNo(orderNo: String, memberNo: String): OrderCompleteHeaderProjection?

    @Query(
        value = """
        WITH ORDER_SUMMARY AS (
            SELECT OB.ORDER_NO
                 , MIN(OD.ORDER_ACCEPT_DTM) AS ORDER_ACCEPT_DTM
                 , SUM(OG.SALE_PRICE * OD.QUANTITY) AS TOTAL_AMOUNT
              FROM ORDER_BASE OB
             INNER JOIN ORDER_DETAIL OD ON OB.ORDER_NO = OD.ORDER_NO
             INNER JOIN ORDER_GOODS OG ON OB.ORDER_NO = OG.ORDER_NO
               AND OD.GOODS_NO = OG.GOODS_NO AND OD.ITEM_NO = OG.ITEM_NO
             WHERE OB.MEMBER_NO = :memberNo
               AND OD.ORDER_TYPE_CODE = '001' AND OD.ORDER_PROCESS_SEQUENCE = 1
             GROUP BY OB.ORDER_NO
        ),
        LATEST_ORDER_PROCESS AS (
            SELECT ORDER_NO, ORDER_SEQUENCE, MAX(ORDER_PROCESS_SEQUENCE) AS MAX_PROCESS_SEQ
              FROM ORDER_DETAIL
             WHERE ORDER_NO IN (SELECT ORDER_NO FROM ORDER_SUMMARY)
             GROUP BY ORDER_NO, ORDER_SEQUENCE
        ),
        CANCELABLE_AMOUNTS AS (
            SELECT PB.ORDER_NO, SUM(PB.CANCELABLE_AMOUNT) AS TOTAL_CANCELABLE_AMOUNT
              FROM PAY_BASE PB
             WHERE PB.ORDER_NO IN (SELECT ORDER_NO FROM ORDER_SUMMARY)
               AND PB.PAY_TYPE_CODE IN ('001', '002')
             GROUP BY PB.ORDER_NO
        )
        SELECT OS.ORDER_NO AS orderNo
             , OS.ORDER_ACCEPT_DTM AS orderAcceptDtm
             , OS.TOTAL_AMOUNT AS totalAmount
             , OD.ORDER_SEQUENCE AS orderSequence
             , OD.ORDER_PROCESS_SEQUENCE AS orderProcessSequence
             , OD.GOODS_NO AS goodsNo
             , OD.ITEM_NO AS itemNo
             , OG.GOODS_NAME AS goodsName
             , OG.ITEM_NAME AS itemName
             , OG.SALE_PRICE AS salePrice
             , OD.QUANTITY AS quantity
             , OD.ORDER_STATUS_CODE AS orderStatusCode
             , CASE OD.ORDER_STATUS_CODE
                   WHEN '001' THEN '주문접수' WHEN '002' THEN '주문완료'
                   WHEN '003' THEN '주문취소' WHEN '107' THEN '배송완료'
                   WHEN '207' THEN '반품완료' ELSE '기타' END AS orderStatusName
             , OD.ORDER_TYPE_CODE AS orderTypeCode
             , CASE OD.ORDER_TYPE_CODE
                   WHEN '001' THEN '주문' WHEN '002' THEN '주문취소'
                   WHEN '101' THEN '반품' WHEN '102' THEN '반품취소'
                   WHEN '201' THEN '교환' WHEN '202' THEN '교환취소'
                   ELSE '기타' END AS orderTypeName
             , CASE WHEN NOT EXISTS (SELECT 1 FROM ORDER_DETAIL OD2
                                      WHERE OD2.ORDER_NO = OD.ORDER_NO
                                        AND OD2.ORDER_SEQUENCE = OD.ORDER_SEQUENCE
                                        AND OD2.ORDER_PROCESS_SEQUENCE > 1)
                     AND OD.ORDER_TYPE_CODE = '001' AND OD.ORDER_PROCESS_SEQUENCE = 1
                    THEN TRUE ELSE FALSE END AS cancelable
             , CASE WHEN NOT EXISTS (SELECT 1 FROM ORDER_DETAIL OD2
                                      WHERE OD2.ORDER_NO = OD.ORDER_NO
                                        AND OD2.ORDER_SEQUENCE = OD.ORDER_SEQUENCE
                                        AND OD2.ORDER_PROCESS_SEQUENCE > 1)
                     AND OD.ORDER_TYPE_CODE = '001' AND OD.ORDER_PROCESS_SEQUENCE = 1
                    THEN COALESCE(CA.TOTAL_CANCELABLE_AMOUNT, 0) ELSE 0 END AS cancelableAmount
          FROM ORDER_SUMMARY OS
         INNER JOIN LATEST_ORDER_PROCESS LOP ON OS.ORDER_NO = LOP.ORDER_NO
         INNER JOIN ORDER_DETAIL OD ON LOP.ORDER_NO = OD.ORDER_NO
           AND LOP.ORDER_SEQUENCE = OD.ORDER_SEQUENCE AND LOP.MAX_PROCESS_SEQ = OD.ORDER_PROCESS_SEQUENCE
         INNER JOIN ORDER_GOODS OG ON OD.ORDER_NO = OG.ORDER_NO
           AND OD.GOODS_NO = OG.GOODS_NO AND OD.ITEM_NO = OG.ITEM_NO
          LEFT JOIN CANCELABLE_AMOUNTS CA ON OS.ORDER_NO = CA.ORDER_NO
         ORDER BY OS.ORDER_ACCEPT_DTM DESC, OD.ORDER_SEQUENCE
        """,
        nativeQuery = true
    )
    fun selectOrderListByMemberNo(memberNo: String): List<OrderListFlatProjection>

    @Query(
        value = """
        SELECT OD.ORDER_SEQUENCE AS orderSequence
             , OD.ORDER_PROCESS_SEQUENCE AS orderProcessSequence
             , OD.GOODS_NO AS goodsNo
             , OD.ITEM_NO AS itemNo
             , OG.GOODS_NAME AS goodsName
             , OG.ITEM_NAME AS itemName
             , OG.SALE_PRICE AS salePrice
             , OD.QUANTITY AS quantity
             , (OG.SALE_PRICE * OD.QUANTITY) AS subtotal
          FROM ORDER_BASE OB
         INNER JOIN ORDER_DETAIL OD ON OB.ORDER_NO = OD.ORDER_NO
         INNER JOIN ORDER_GOODS OG ON OD.ORDER_NO = OG.ORDER_NO
           AND OD.GOODS_NO = OG.GOODS_NO AND OD.ITEM_NO = OG.ITEM_NO
         WHERE OB.ORDER_NO = :orderNo AND OB.MEMBER_NO = :memberNo
           AND OD.ORDER_TYPE_CODE = '001' AND OD.ORDER_PROCESS_SEQUENCE = 1
           AND NOT EXISTS (SELECT 1 FROM ORDER_DETAIL OD2
                            WHERE OD2.ORDER_NO = OD.ORDER_NO
                              AND OD2.ORDER_SEQUENCE = OD.ORDER_SEQUENCE
                              AND OD2.ORDER_PROCESS_SEQUENCE > 1)
         ORDER BY OD.ORDER_SEQUENCE
        """,
        nativeQuery = true
    )
    fun selectCancelableOrdersByOrderNo(
        orderNo: String, memberNo: String
    ): List<CancelableOrderItemProjection>

    @Query(
        value = """
        SELECT PB.PAY_WAY_CODE AS payWayCode
             , CASE PB.PAY_WAY_CODE WHEN '001' THEN '신용카드' WHEN '002' THEN '포인트' ELSE '기타' END AS payWayName
             , SUM(PB.CANCELABLE_AMOUNT) AS refundAmount
             , PB.PG_TYPE_CODE AS pgTypeCode
             , CASE PB.PG_TYPE_CODE WHEN '001' THEN 'KG이니시스' WHEN '002' THEN '나이스페이' ELSE NULL END AS pgTypeName
          FROM PAY_BASE PB
         WHERE PB.ORDER_NO = :orderNo
           AND PB.PAY_TYPE_CODE IN ('001', '002')
           AND PB.CANCELABLE_AMOUNT > 0
         GROUP BY PB.PAY_WAY_CODE, PB.PG_TYPE_CODE
         ORDER BY CASE PB.PAY_WAY_CODE WHEN '001' THEN 1 WHEN '002' THEN 2 ELSE 3 END
        """,
        nativeQuery = true
    )
    fun selectRefundDetailsByOrderNo(orderNo: String): List<RefundDetailProjection>
}
