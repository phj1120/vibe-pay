package com.api.app.repository.rodb.basket

import com.api.app.entity.QBasketBase
import com.api.app.entity.QGoodsBase
import com.api.app.entity.QGoodsItem
import com.api.app.entity.QGoodsPriceHist
import com.api.app.vo.BasketVo
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.beans.factory.annotation.Qualifier
import java.time.LocalDateTime

class BasketBaseRepositoryImpl(
    @Qualifier("roJpaQueryFactory") private val queryFactory: JPAQueryFactory
) : BasketBaseRepositoryCustom {

    private val qBasketBase = QBasketBase.basketBase
    private val qGoodsBase = QGoodsBase.goodsBase
    private val qGoodsItem = QGoodsItem.goodsItem
    private val qGoodsPriceHist = QGoodsPriceHist.goodsPriceHist

    override fun selectBasketListByMemberNo(memberNo: String): List<BasketVo> {
        val now = LocalDateTime.now()
        return buildBasketQuery(now)
            .where(
                qBasketBase.memberNo.eq(memberNo),
                qBasketBase.isOrder.isFalse
            )
            .orderBy(qBasketBase.registDateTime.desc())
            .fetch()
    }

    override fun selectBasketListByBasketNos(basketNos: List<String>): List<BasketVo> {
        val now = LocalDateTime.now()
        return buildBasketQuery(now)
            .where(qBasketBase.basketNo.`in`(basketNos))
            .orderBy(qBasketBase.registDateTime.desc())
            .fetch()
    }

    private fun buildBasketQuery(now: LocalDateTime) = queryFactory
        .select(Projections.constructor(
            BasketVo::class.java,
            qBasketBase.basketNo,
            qBasketBase.memberNo,
            qBasketBase.goodsNo,
            qGoodsBase.goodsName,
            qGoodsBase.goodsStatusCode,
            qGoodsBase.goodsMainImageUrl,
            Expressions.numberTemplate(Long::class.java, "coalesce({0}, 0) + {1}",
                qGoodsPriceHist.salePrice, qGoodsItem.itemPrice),
            qBasketBase.itemNo,
            qGoodsItem.itemName,
            qGoodsItem.itemPrice,
            qGoodsItem.goodsStatusCode,
            qGoodsItem.stock,
            qBasketBase.quantity,
            qBasketBase.isOrder,
            qBasketBase.registDateTime
        ))
        .from(qBasketBase)
        .innerJoin(qGoodsBase).on(qGoodsBase.goodsNo.eq(qBasketBase.goodsNo))
        .innerJoin(qGoodsItem).on(
            qGoodsItem.id.goodsNo.eq(qBasketBase.goodsNo),
            qGoodsItem.id.itemNo.eq(qBasketBase.itemNo)
        )
        .leftJoin(qGoodsPriceHist).on(
            qGoodsPriceHist.id.goodsNo.eq(qBasketBase.goodsNo),
            qGoodsPriceHist.id.startDateTime.loe(now),
            qGoodsPriceHist.endDateTime.goe(now)
        )
}
