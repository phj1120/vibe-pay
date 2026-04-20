package com.api.app.emum

interface CommonCode {
    val code: String
    val codeName: String
    val displaySequence: Int
    val referenceValue1: String
    val referenceValue2: String

    fun isEquals(code: String): Boolean = this.code == code
}
