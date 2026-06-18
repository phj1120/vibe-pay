package com.api.app.repository.rodb.order

import com.api.app.entity.QOrderDetail
import com.api.app.entity.QOrderGoods
import com.api.app.vo.OrderCompleteGoodsVo
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.beans.factory.annotation.Qualifier

class OrderGoodsRepositoryImpl(
    @Qualifier("secondaryJpaQueryFactory") private val queryFactory: JPAQueryFactory
) : OrderGoodsRepositoryCustom {

    private val qOrderGoods = QOrderGoods.orderGoods
    private val qOrderDetail = QOrderDetail.orderDetail

    override fun selectOrderCompleteGoodsByOrderNo(orderNo: String): List<OrderCompleteGoodsVo> {
        val subtotal = qOrderGoods.salePrice.multiply(qOrderDetail.quantity)
        return queryFactory
            .select(
                Projections.constructor(
                    OrderCompleteGoodsVo::class.java,
                    qOrderGoods.id.goodsNo,
                    qOrderGoods.id.itemNo,
                    qOrderGoods.goodsName,
                    qOrderGoods.itemName,
                    qOrderGoods.salePrice,
                    qOrderDetail.quantity,
                    subtotal
                )
            )
            .from(qOrderGoods)
            .innerJoin(qOrderDetail).on(
                qOrderDetail.id.orderNo.eq(qOrderGoods.id.orderNo),
                qOrderDetail.goodsNo.eq(qOrderGoods.id.goodsNo),
                qOrderDetail.itemNo.eq(qOrderGoods.id.itemNo)
            )
            .where(
                qOrderGoods.id.orderNo.eq(orderNo),
                qOrderDetail.orderTypeCode.eq("001"),
                qOrderDetail.id.orderProcessSequence.eq(1L)
            )
            .orderBy(qOrderDetail.id.orderSequence.asc())
            .fetch()
    }
}
