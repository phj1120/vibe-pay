package com.api.app.dto.response.payment

import com.fasterxml.jackson.annotation.JsonProperty

data class InicisApprovalResponse(
    val resultCode: String? = null,
    val resultMsg: String? = null,
    val applNum: String? = null,
    val tid: String? = null,
    @JsonProperty("TotPrice") val totPrice: String? = null,
    @JsonProperty("CARD_Num") val cardNum: String? = null,
    @JsonProperty("CARD_Code") val cardCode: String? = null,
    @JsonProperty("CARD_BankCode") val cardBankCode: String? = null,
    @JsonProperty("CARD_Quota") val cardQuota: String? = null,
    val applDate: String? = null,
    val mid: String? = null,
    val oid: String? = null
)
