package com.api.app.repository.rodb.point

import com.api.app.entity.PointHistory
import com.api.app.entity.QPointHistory
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.beans.factory.annotation.Qualifier
import java.time.LocalDateTime

class PointHistoryRepositoryImpl(
    @Qualifier("roJpaQueryFactory") private val queryFactory: JPAQueryFactory
) : PointHistoryRepositoryCustom {

    private val qPointHistory = QPointHistory.pointHistory

    override fun selectPointBalance(memberNo: String): Long {
        return queryFactory
            .select(qPointHistory.remainPoint.sum())
            .from(qPointHistory)
            .where(
                qPointHistory.memberNo.eq(memberNo),
                qPointHistory.pointTransactionCode.eq("001"),
                qPointHistory.remainPoint.gt(0L),
                qPointHistory.endDateTime.gt(LocalDateTime.now())
            )
            .fetchOne() ?: 0L
    }

    override fun selectPointHistoryList(memberNo: String, size: Int, offset: Long): List<PointHistory> {
        return queryFactory
            .selectFrom(qPointHistory)
            .where(qPointHistory.memberNo.eq(memberNo))
            .orderBy(qPointHistory.registDateTime.desc())
            .limit(size.toLong())
            .offset(offset)
            .fetch()
    }

    override fun countPointHistory(memberNo: String): Long {
        return queryFactory
            .select(qPointHistory.count())
            .from(qPointHistory)
            .where(qPointHistory.memberNo.eq(memberNo))
            .fetchOne() ?: 0L
    }

    override fun selectAvailablePointHistory(memberNo: String): List<PointHistory> {
        return queryFactory
            .selectFrom(qPointHistory)
            .where(
                qPointHistory.memberNo.eq(memberNo),
                qPointHistory.pointTransactionCode.eq("001"),
                qPointHistory.remainPoint.gt(0L),
                qPointHistory.endDateTime.gt(LocalDateTime.now())
            )
            .orderBy(qPointHistory.endDateTime.asc())
            .fetch()
    }
}
