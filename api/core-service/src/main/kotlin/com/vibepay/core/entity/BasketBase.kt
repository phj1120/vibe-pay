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
@Table(name = "BASKET_BASE")
class BasketBase : PersistableSequenceEntity() {

    @Id
    @GeneratedValue(generator = "basket_no_gen")
    @GenericGenerator(
        name = "basket_no_gen",
        type = PaddedSequenceIdGenerator::class,
        parameters = [
            Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_BASKET_NO"),
            Parameter(name = "pad_length", value = "15")
        ]
    )
    @Column(name = "BASKET_NO", length = 15, nullable = false)
    var basketNo: String = ""

    override fun getId() = basketNo

    @Column(name = "MEMBER_NO", nullable = false, length = 15)
    var memberNo: String = ""

    @Column(name = "GOODS_NO", nullable = false, length = 15)
    var goodsNo: String = ""

    @Column(name = "ITEM_NO", nullable = false, length = 3)
    var itemNo: String = ""

    @Column(name = "QUANTITY", nullable = false, columnDefinition = "numeric")
    var quantity: Long = 0

    @Column(name = "IS_ORDER", nullable = false)
    var isOrder: Boolean = false
}
