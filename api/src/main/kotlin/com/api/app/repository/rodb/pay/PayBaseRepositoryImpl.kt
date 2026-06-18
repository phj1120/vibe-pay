package com.api.app.repository.rodb.pay

import com.api.app.entity.QPayBase
import com.api.app.vo.OrderCompletePaymentVo
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.beans.factory.annotation.Qualifier

class PayBaseRepositoryImpl(
    @Qualifier("roJpaQueryFactory") private val queryFactory: JPAQueryFactory
) : PayBaseRepositoryCustom {

    private val qPayBase = QPayBase.payBase

    override fun selectOrderCompletePaymentByOrderNo(orderNo: String): List<OrderCompletePaymentVo> {
        return queryFactory
            .select(Projections.constructor(
                OrderCompletePaymentVo::class.java,
                qPayBase.payWayCode,
                qPayBase.amount,
                qPayBase.pgTypeCode
            ))
            .from(qPayBase)
            .where(
                qPayBase.orderNo.eq(orderNo),
                qPayBase.payTypeCode.eq("001"),
                qPayBase.claimNo.isNull
            )
            .orderBy(qPayBase.payNo.asc())
            .fetch()
    }
}
