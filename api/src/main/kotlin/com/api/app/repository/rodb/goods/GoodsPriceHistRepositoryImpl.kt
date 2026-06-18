package com.api.app.repository.rodb.goods

import com.api.app.entity.GoodsPriceHist
import com.api.app.entity.QGoodsPriceHist
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.beans.factory.annotation.Qualifier
import java.time.LocalDateTime

class GoodsPriceHistRepositoryImpl(
    @Qualifier("roJpaQueryFactory") private val queryFactory: JPAQueryFactory
) : GoodsPriceHistRepositoryCustom {

    private val qGoodsPriceHist = QGoodsPriceHist.goodsPriceHist

    override fun selectCurrentPrice(goodsNo: String): GoodsPriceHist? {
        val now = LocalDateTime.now()
        return queryFactory
            .selectFrom(qGoodsPriceHist)
            .where(
                qGoodsPriceHist.id.goodsNo.eq(goodsNo),
                qGoodsPriceHist.id.startDateTime.loe(now),
                qGoodsPriceHist.endDateTime.goe(now)
            )
            .fetchFirst()
    }
}
