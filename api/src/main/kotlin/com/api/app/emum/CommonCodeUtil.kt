package com.api.app.emum

object CommonCodeUtil {
    fun <T> findByCode(enumClass: Class<T>, code: String): T? where T : Enum<T>, T : CommonCode {
        return enumClass.enumConstants?.firstOrNull { it.code == code }
    }
}
