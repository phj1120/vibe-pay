package com.api.app.entity

import com.api.app.common.id.PaddedSequenceIdGenerator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.id.enhanced.SequenceStyleGenerator

@Entity
@Table(name = "GOODS_BASE")
class GoodsBase : PersistableSequenceEntity() {

    @Id
    @GeneratedValue(generator = "goods_no_gen")
    @GenericGenerator(
        name = "goods_no_gen",
        type = PaddedSequenceIdGenerator::class,
        parameters = [
            Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SEQ_GOODS_NO"),
            Parameter(name = "pad_length", value = "14"),
            Parameter(name = "prefix", value = "G")
        ]
    )
    @Column(name = "GOODS_NO", length = 15, nullable = false)
    var goodsNo: String = ""

    override fun getId() = goodsNo

    @Column(name = "GOODS_NAME", nullable = false, length = 200)
    var goodsName: String = ""

    @Column(name = "GOODS_STATUS_CODE", nullable = false, length = 3)
    var goodsStatusCode: String = ""

    @Column(name = "GOODS_MAIN_IMAGE_URL", length = 500)
    var goodsMainImageUrl: String? = null
}
