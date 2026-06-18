package com.api.app.repository.rodb.order

import com.api.app.entity.QOrderBase
import com.api.app.entity.QOrderDetail
import com.api.app.entity.QOrderGoods
import com.api.app.entity.QPayBase
import com.api.app.vo.CancelableOrderItemVo
import com.api.app.vo.OrderCompleteHeaderVo
import com.api.app.vo.OrderListFlatVo
import com.api.app.vo.RefundDetailVo
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.beans.factory.annotation.Qualifier

class OrderBaseRepositoryImpl(
    @Qualifier("secondaryJpaQueryFactory") private val queryFactory: JPAQueryFactory
) : OrderBaseRepositoryCustom {

    private val qOrderBase = QOrderBase.orderBase
    private val qOrderDetail = QOrderDetail.orderDetail
    private val qOrderGoods = QOrderGoods.orderGoods
    private val qPayBase = QPayBase.payBase

    override fun selectOrderCompleteByOrderNo(orderNo: String, memberNo: String): OrderCompleteHeaderVo? {
        val minAcceptDtm = qOrderDetail.orderAcceptDtm.min()
        val totalAmount = qOrderGoods.salePrice.multiply(qOrderDetail.quantity).sum()
        val row = queryFactory
            .select(qOrderBase.orderNo, qOrderBase.memberNo, minAcceptDtm, totalAmount)
            .from(qOrderBase)
            .innerJoin(qOrderDetail).on(
                qOrderDetail.id.orderNo.eq(qOrderBase.orderNo),
                qOrderDetail.orderTypeCode.eq("001"),
                qOrderDetail.id.orderProcessSequence.eq(1L)
            )
            .innerJoin(qOrderGoods).on(
                qOrderGoods.id.orderNo.eq(qOrderBase.orderNo),
                qOrderGoods.id.goodsNo.eq(qOrderDetail.goodsNo),
                qOrderGoods.id.itemNo.eq(qOrderDetail.itemNo)
            )
            .where(
                qOrderBase.orderNo.eq(orderNo),
                qOrderBase.memberNo.eq(memberNo)
            )
            .groupBy(qOrderBase.orderNo, qOrderBase.memberNo)
            .fetchOne()
            ?: return null

        return OrderCompleteHeaderVo(
            orderNo = row.get(qOrderBase.orderNo),
            memberNo = row.get(qOrderBase.memberNo),
            orderAcceptDtm = row.get(minAcceptDtm),
            totalAmount = row.get(totalAmount)
        )
    }

    override fun selectOrderListByMemberNo(memberNo: String): List<OrderListFlatVo> {
        val minAcceptDtm = qOrderDetail.orderAcceptDtm.min()
        val totalAmtExpr = qOrderGoods.salePrice.multiply(qOrderDetail.quantity).sum()

        val summaryRows = queryFactory
            .select(qOrderBase.orderNo, minAcceptDtm, totalAmtExpr)
            .from(qOrderBase)
            .innerJoin(qOrderDetail).on(
                qOrderDetail.id.orderNo.eq(qOrderBase.orderNo),
                qOrderDetail.orderTypeCode.eq("001"),
                qOrderDetail.id.orderProcessSequence.eq(1L)
            )
            .innerJoin(qOrderGoods).on(
                qOrderGoods.id.orderNo.eq(qOrderBase.orderNo),
                qOrderGoods.id.goodsNo.eq(qOrderDetail.goodsNo),
                qOrderGoods.id.itemNo.eq(qOrderDetail.itemNo)
            )
            .where(qOrderBase.memberNo.eq(memberNo))
            .groupBy(qOrderBase.orderNo)
            .fetch()

        if (summaryRows.isEmpty()) return emptyList()

        val orderNos = summaryRows.mapNotNull { it.get(qOrderBase.orderNo) }
        val summaryMap = summaryRows.associate { row ->
            (row.get(qOrderBase.orderNo) ?: "") to
                Pair(row.get(minAcceptDtm), row.get(totalAmtExpr))
        }

        val qOd2 = QOrderDetail("od2")
        val detailRows = queryFactory
            .select(
                qOrderDetail.id.orderNo,
                qOrderDetail.id.orderSequence,
                qOrderDetail.id.orderProcessSequence,
                qOrderDetail.goodsNo,
                qOrderDetail.itemNo,
                qOrderGoods.goodsName,
                qOrderGoods.itemName,
                qOrderGoods.salePrice,
                qOrderDetail.quantity,
                qOrderDetail.orderStatusCode,
                qOrderDetail.orderTypeCode
            )
            .from(qOrderDetail)
            .innerJoin(qOrderGoods).on(
                qOrderGoods.id.orderNo.eq(qOrderDetail.id.orderNo),
                qOrderGoods.id.goodsNo.eq(qOrderDetail.goodsNo),
                qOrderGoods.id.itemNo.eq(qOrderDetail.itemNo)
            )
            .where(
                qOrderDetail.id.orderNo.`in`(orderNos),
                qOrderDetail.id.orderProcessSequence.eq(
                    JPAExpressions.select(qOd2.id.orderProcessSequence.max())
                        .from(qOd2)
                        .where(
                            qOd2.id.orderNo.eq(qOrderDetail.id.orderNo),
                            qOd2.id.orderSequence.eq(qOrderDetail.id.orderSequence)
                        )
                )
            )
            .fetch()

        val cancelAmtExpr = qPayBase.cancelableAmount.sum()
        val cancelableRows = queryFactory
            .select(qPayBase.orderNo, cancelAmtExpr)
            .from(qPayBase)
            .where(
                qPayBase.orderNo.`in`(orderNos),
                qPayBase.payTypeCode.`in`(listOf("001", "002"))
            )
            .groupBy(qPayBase.orderNo)
            .fetch()
        val cancelableAmtMap = cancelableRows.associate { row ->
            (row.get(qPayBase.orderNo) ?: "") to (row.get(cancelAmtExpr) ?: 0L)
        }

        return detailRows.map { row ->
            val orderNo = row.get(qOrderDetail.id.orderNo) ?: ""
            val processSeq = row.get(qOrderDetail.id.orderProcessSequence) ?: 0L
            val typeCode = row.get(qOrderDetail.orderTypeCode) ?: ""
            val statusCode = row.get(qOrderDetail.orderStatusCode) ?: ""
            val cancelable = processSeq == 1L && typeCode == "001"

            OrderListFlatVo(
                orderNo = orderNo,
                orderAcceptDtm = summaryMap[orderNo]?.first,
                totalAmount = summaryMap[orderNo]?.second,
                orderSequence = row.get(qOrderDetail.id.orderSequence),
                orderProcessSequence = processSeq,
                goodsNo = row.get(qOrderDetail.goodsNo),
                itemNo = row.get(qOrderDetail.itemNo),
                goodsName = row.get(qOrderGoods.goodsName),
                itemName = row.get(qOrderGoods.itemName),
                salePrice = row.get(qOrderGoods.salePrice),
                quantity = row.get(qOrderDetail.quantity),
                orderStatusCode = statusCode,
                orderStatusName = when (statusCode) {
                    "001" -> "주문접수"
                    "002" -> "주문완료"
                    "003" -> "주문취소"
                    "107" -> "배송완료"
                    "207" -> "반품완료"
                    else -> "기타"
                },
                orderTypeCode = typeCode,
                orderTypeName = when (typeCode) {
                    "001" -> "주문"
                    "002" -> "주문취소"
                    "101" -> "반품"
                    "102" -> "반품취소"
                    "201" -> "교환"
                    "202" -> "교환취소"
                    else -> "기타"
                },
                cancelable = cancelable,
                cancelableAmount = if (cancelable) cancelableAmtMap[orderNo] ?: 0L else 0L
            )
        }.sortedWith(
            compareByDescending<OrderListFlatVo> { summaryMap[it.orderNo ?: ""]?.first }
                .thenBy { it.orderSequence }
        )
    }

    override fun selectCancelableOrdersByOrderNo(orderNo: String, memberNo: String): List<CancelableOrderItemVo> {
        val subtotal = qOrderGoods.salePrice.multiply(qOrderDetail.quantity)
        val qOd2 = QOrderDetail("od2")
        return queryFactory
            .select(
                Projections.constructor(
                    CancelableOrderItemVo::class.java,
                    qOrderDetail.id.orderSequence,
                    qOrderDetail.id.orderProcessSequence,
                    qOrderDetail.goodsNo,
                    qOrderDetail.itemNo,
                    qOrderGoods.goodsName,
                    qOrderGoods.itemName,
                    qOrderGoods.salePrice,
                    qOrderDetail.quantity,
                    subtotal
                )
            )
            .from(qOrderBase)
            .innerJoin(qOrderDetail).on(
                qOrderDetail.id.orderNo.eq(qOrderBase.orderNo),
                qOrderDetail.orderTypeCode.eq("001"),
                qOrderDetail.id.orderProcessSequence.eq(1L)
            )
            .innerJoin(qOrderGoods).on(
                qOrderGoods.id.orderNo.eq(qOrderDetail.id.orderNo),
                qOrderGoods.id.goodsNo.eq(qOrderDetail.goodsNo),
                qOrderGoods.id.itemNo.eq(qOrderDetail.itemNo)
            )
            .where(
                qOrderBase.orderNo.eq(orderNo),
                qOrderBase.memberNo.eq(memberNo),
                JPAExpressions.selectOne()
                    .from(qOd2)
                    .where(
                        qOd2.id.orderNo.eq(qOrderDetail.id.orderNo),
                        qOd2.id.orderSequence.eq(qOrderDetail.id.orderSequence),
                        qOd2.id.orderProcessSequence.gt(1L)
                    )
                    .notExists()
            )
            .orderBy(qOrderDetail.id.orderSequence.asc())
            .fetch()
    }

    override fun selectRefundDetailsByOrderNo(orderNo: String): List<RefundDetailVo> {
        val refundAmtExpr = qPayBase.cancelableAmount.sum()
        val rows = queryFactory
            .select(qPayBase.payWayCode, refundAmtExpr, qPayBase.pgTypeCode)
            .from(qPayBase)
            .where(
                qPayBase.orderNo.eq(orderNo),
                qPayBase.payTypeCode.`in`(listOf("001", "002")),
                qPayBase.cancelableAmount.gt(0L)
            )
            .groupBy(qPayBase.payWayCode, qPayBase.pgTypeCode)
            .orderBy(
                CaseBuilder()
                    .`when`(qPayBase.payWayCode.eq("001")).then(1)
                    .`when`(qPayBase.payWayCode.eq("002")).then(2)
                    .otherwise(3).asc()
            )
            .fetch()

        return rows.map { row ->
            val payWayCode = row.get(qPayBase.payWayCode)
            val pgTypeCode = row.get(qPayBase.pgTypeCode)
            RefundDetailVo(
                payWayCode = payWayCode,
                payWayName = when (payWayCode) {
                    "001" -> "신용카드"
                    "002" -> "포인트"
                    else -> "기타"
                },
                refundAmount = row.get(refundAmtExpr),
                pgTypeCode = pgTypeCode,
                pgTypeName = when (pgTypeCode) {
                    "001" -> "KG이니시스"
                    "002" -> "나이스페이"
                    "999" -> "테스트PG"
                    else -> null
                }
            )
        }
    }
}
