package com.vibepay.core.entity

import com.api.app.common.id.PaddedSequenceIdGenerator
import com.api.app.entity.PersistableSequenceEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.id.enhanced.SequenceStyleGenerator
import java.time.LocalDateTime

@Entity
@Table(name = "POINT_HISTORY")
class PointHistory : PersistableSequenceEntity() {

    @Id
    @GeneratedValue(generator = "point_history_no_gen")
    @GenericGenerator(
        name = "point_history_no_gen",
        type = PaddedSequenceIdGenerator::class,
        parameters = [
            Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_POINT_HISTORY_NO"),
            Parameter(name = "pad_length", value = "15")
        ]
    )
    @Column(name = "POINT_HISTORY_NO", length = 15, nullable = false)
    var pointHistoryNo: String = ""

    override fun getId() = pointHistoryNo

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
