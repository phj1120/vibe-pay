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

@Entity
@Table(name = "MEMBER_BASE")
class MemberBase : PersistableSequenceEntity() {

    @Id
    @GeneratedValue(generator = "member_no_gen")
    @GenericGenerator(
        name = "member_no_gen",
        type = PaddedSequenceIdGenerator::class,
        parameters = [
            Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_MEMBER_NO"),
            Parameter(name = "pad_length", value = "15")
        ]
    )
    @Column(name = "MEMBER_NO", length = 15, nullable = false)
    var memberNo: String = ""

    override fun getId() = memberNo

    @Column(name = "MEMBER_NAME", nullable = false, length = 30)
    var memberName: String = ""

    @Column(name = "PHONE", length = 20)
    var phone: String? = null

    @Column(name = "EMAIL", nullable = false, length = 100)
    var email: String = ""

    @Column(name = "PASSWORD", nullable = false, length = 200)
    var password: String = ""

    @Column(name = "MEMBER_STATUS_CODE", nullable = false, length = 3)
    var memberStatusCode: String = ""
}
