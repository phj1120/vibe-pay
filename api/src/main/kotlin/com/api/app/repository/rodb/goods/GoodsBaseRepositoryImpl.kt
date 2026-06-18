package com.api.app.repository.rodb.goods

import com.api.app.entity.QCodeDetail
import com.api.app.entity.QGoodsBase
import com.api.app.entity.QGoodsPriceHist
import com.api.app.vo.GoodsDetailVo
import com.api.app.vo.GoodsListVo
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.beans.factory.annotation.Qualifier
import java.time.LocalDateTime

class GoodsBaseRepositoryImpl(
    @Qualifier("roJpaQueryFactory") private val queryFactory: JPAQueryFactory
) : GoodsBaseRepositoryCustom {

    private val qGoodsBase = QGoodsBase.goodsBase
    private val qGoodsPriceHist = QGoodsPriceHist.goodsPriceHist
    private val qCodeDetail = QCodeDetail.codeDetail

    override fun selectGoodsList(goodsStatusCode: String?, goodsName: String?, size: Int, offset: Long): List<GoodsListVo> {
        val now = LocalDateTime.now()
        val builder = BooleanBuilder()
        goodsStatusCode?.let { builder.and(qGoodsBase.goodsStatusCode.eq(it)) }
        goodsName?.let { builder.and(qGoodsBase.goodsName.contains(it)) }

        return queryFactory
            .select(Projections.constructor(
                GoodsListVo::class.java,
                qGoodsBase.goodsNo,
                qGoodsBase.goodsName,
                qGoodsBase.goodsStatusCode,
                qCodeDetail.codeName,
                qGoodsBase.goodsMainImageUrl,
                qGoodsPriceHist.salePrice,
                qGoodsPriceHist.supplyPrice
            ))
            .from(qGoodsBase)
            .innerJoin(qGoodsPriceHist).on(
                qGoodsPriceHist.id.goodsNo.eq(qGoodsBase.goodsNo),
                qGoodsPriceHist.id.startDateTime.loe(now),
                qGoodsPriceHist.endDateTime.goe(now)
            )
            .leftJoin(qCodeDetail).on(
                qCodeDetail.id.groupCode.eq("PRD001"),
                qCodeDetail.id.code.eq(qGoodsBase.goodsStatusCode)
            )
            .where(builder)
            .orderBy(qGoodsBase.registDateTime.desc())
            .limit(size.toLong())
            .offset(offset)
            .fetch()
    }

    override fun countGoodsList(goodsStatusCode: String?, goodsName: String?): Long {
        val builder = BooleanBuilder()
        goodsStatusCode?.let { builder.and(qGoodsBase.goodsStatusCode.eq(it)) }
        goodsName?.let { builder.and(qGoodsBase.goodsName.contains(it)) }

        return queryFactory
            .select(qGoodsBase.count())
            .from(qGoodsBase)
            .where(builder)
            .fetchOne() ?: 0L
    }

    override fun selectGoodsDetail(goodsNo: String): GoodsDetailVo? {
        val now = LocalDateTime.now()
        return queryFactory
            .select(Projections.constructor(
                GoodsDetailVo::class.java,
                qGoodsBase.goodsNo,
                qGoodsBase.goodsName,
                qGoodsBase.goodsStatusCode,
                qCodeDetail.codeName,
                qGoodsBase.goodsMainImageUrl,
                qGoodsPriceHist.salePrice,
                qGoodsPriceHist.supplyPrice,
                qGoodsBase.registDateTime,
                qGoodsBase.modifyDateTime
            ))
            .from(qGoodsBase)
            .innerJoin(qGoodsPriceHist).on(
                qGoodsPriceHist.id.goodsNo.eq(qGoodsBase.goodsNo),
                qGoodsPriceHist.id.startDateTime.loe(now),
                qGoodsPriceHist.endDateTime.goe(now)
            )
            .leftJoin(qCodeDetail).on(
                qCodeDetail.id.groupCode.eq("PRD001"),
                qCodeDetail.id.code.eq(qGoodsBase.goodsStatusCode)
            )
            .where(qGoodsBase.goodsNo.eq(goodsNo))
            .fetchOne()
    }
}
