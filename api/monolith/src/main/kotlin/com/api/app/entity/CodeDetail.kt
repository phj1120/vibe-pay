package com.api.app.entity

import jakarta.persistence.*
import java.io.Serializable

@Embeddable
data class CodeDetailId(
    @Column(name = "GROUP_CODE", length = 15) val groupCode: String = "",
    @Column(name = "CODE", length = 3) val code: String = ""
) : Serializable

@Entity
@Table(name = "CODE_DETAIL")
class CodeDetail : SystemEntity() {

    @EmbeddedId
    var id: CodeDetailId = CodeDetailId()

    @Column(name = "CODE_NAME", nullable = false, length = 100)
    var codeName: String = ""

    @Column(name = "REFERENCE_VALUE_1", length = 255)
    var referenceValue1: String? = null

    @Column(name = "REFERENCE_VALUE_2", length = 255)
    var referenceValue2: String? = null

    @Column(name = "DISPLAY_SEQUENCE", columnDefinition = "numeric")
    var displaySequence: Long = 0

    // Convenience accessors
    val groupCode: String get() = id.groupCode
    val code: String get() = id.code
}
