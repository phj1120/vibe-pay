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

@Entity
@Table(name = "PAY_INTERFACE_LOG")
class PayInterfaceLog : PersistableSequenceEntity() {

    @Id
    @GeneratedValue(generator = "pay_interface_no_gen")
    @GenericGenerator(
        name = "pay_interface_no_gen",
        type = PaddedSequenceIdGenerator::class,
        parameters = [
            Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_PAY_INTERFACE_NO"),
            Parameter(name = "pad_length", value = "15")
        ]
    )
    @Column(name = "PAY_INTERFACE_NO", length = 15, nullable = false)
    var payInterfaceNo: String = ""

    override fun getId() = payInterfaceNo

    @Column(name = "MEMBER_NO", nullable = false, length = 15)
    var memberNo: String = ""

    @Column(name = "PAY_NO", nullable = false, length = 15)
    var payNo: String = ""

    @Column(name = "PAY_LOG_CODE", nullable = false, length = 3)
    var payLogCode: String = ""

    @Column(name = "REQUEST_JSON", columnDefinition = "TEXT")
    var requestJson: String? = null

    @Column(name = "RESPONSE_JSON", columnDefinition = "TEXT")
    var responseJson: String? = null
}
