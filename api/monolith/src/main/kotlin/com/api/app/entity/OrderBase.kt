package com.api.app.entity

import com.api.app.common.id.DatePrefixedSequenceIdGenerator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.id.enhanced.SequenceStyleGenerator

@Entity
@Table(name = "ORDER_BASE")
class OrderBase : PersistableSequenceEntity() {

    @Id
    @GeneratedValue(generator = "order_no_gen")
    @GenericGenerator(
        name = "order_no_gen",
        type = DatePrefixedSequenceIdGenerator::class,
        parameters = [
            Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_ORDER_NO"),
            Parameter(name = "letter", value = "O"),
            Parameter(name = "seq_length", value = "6")
        ]
    )
    @Column(name = "ORDER_NO", length = 15, nullable = false)
    var orderNo: String = ""

    override fun getId() = orderNo

    @Column(name = "MEMBER_NO", nullable = false, length = 15)
    var memberNo: String = ""

    @Column(name = "ORDER_STATUS_CODE", length = 10)
    var orderStatusCode: String = "PENDING"
}
