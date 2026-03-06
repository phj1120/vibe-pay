package com.api.app.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "POINT_HISTORY")
class PointHistory : SystemEntity() {

    @Id
    @Column(name = "POINT_HISTORY_NO", length = 15, nullable = false)
    var pointHistoryNo: String = ""

    @Column(name = "MEMBER_NO", nullable = false, length = 15)
    var memberNo: String = ""

    @Column(name = "AMOUNT", nullable = false, columnDefinition = "numeric")
    var amount: Long = 0

    @Column(name = "POINT_TRANSACTION_CODE", nullable = false, length = 3)
    var pointTransactionCode: String = ""

    @Column(name = "POINT_TRANSACTION_REASON_CODE", nullable = false, length = 3)
    var pointTransactionReasonCode: String = ""

    @Column(name = "POINT_TRANSACTION_REASON_NO", length = 15)
    var pointTransactionReasonNo: String? = null

    @Column(name = "START_DATE_TIME")
    var startDateTime: LocalDateTime? = null

    @Column(name = "END_DATE_TIME")
    var endDateTime: LocalDateTime? = null

    @Column(name = "UPPER_POINT_HISTORY_NO", length = 15)
    var upperPointHistoryNo: String? = null

    @Column(name = "REMAIN_POINT", columnDefinition = "numeric")
    var remainPoint: Long = 0
}
