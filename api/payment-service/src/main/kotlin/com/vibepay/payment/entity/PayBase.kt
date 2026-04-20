package com.vibepay.payment.entity

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
@Table(name = "PAY_BASE")
class PayBase : PersistableSequenceEntity() {

    @Id
    @GeneratedValue(generator = "pay_no_gen")
    @GenericGenerator(
        name = "pay_no_gen",
        type = PaddedSequenceIdGenerator::class,
        parameters = [
            Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_PAY_NO"),
            Parameter(name = "pad_length", value = "15")
        ]
    )
    @Column(name = "PAY_NO", length = 15, nullable = false)
    var payNo: String = ""

    override fun getId() = payNo

    @Column(name = "PAY_TYPE_CODE", nullable = false, length = 3)
    var payTypeCode: String = ""

    @Column(name = "PAY_WAY_CODE", nullable = false, length = 3)
    var payWayCode: String = ""

    @Column(name = "PAY_STATUS_CODE", nullable = false, length = 3)
    var payStatusCode: String = ""

    @Column(name = "APPROVE_NO", length = 50)
    var approveNo: String? = null

    @Column(name = "ORDER_NO", length = 15)
    var orderNo: String? = null

    @Column(name = "CLAIM_NO", length = 15)
    var claimNo: String? = null

    @Column(name = "UPPER_PAY_NO", length = 15)
    var upperPayNo: String? = null

    @Column(name = "TRD_NO", length = 100)
    var trdNo: String? = null

    @Column(name = "PAY_FINISH_DATE_TIME")
    var payFinishDateTime: LocalDateTime? = null

    @Column(name = "MEMBER_NO", nullable = false, length = 15)
    var memberNo: String = ""

    @Column(name = "AMOUNT", nullable = false, columnDefinition = "numeric")
    var amount: Long = 0

    @Column(name = "CANCELABLE_AMOUNT", columnDefinition = "numeric")
    var cancelableAmount: Long? = null

    @Column(name = "PG_TYPE_CODE", length = 3)
    var pgTypeCode: String? = null
}
