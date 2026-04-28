package com.api.app.dto.request.payment

import com.fasterxml.jackson.annotation.JsonProperty

data class NiceApprovalRequest(
    @JsonProperty("TID") val tid: String? = null,
    @JsonProperty("AuthToken") val authToken: String? = null,
    @JsonProperty("MID") val mid: String? = null,
    @JsonProperty("Amt") val amt: String? = null,
    @JsonProperty("EdiDate") val ediDate: String? = null,
    @JsonProperty("SignData") val signData: String? = null,
    @JsonProperty("CharSet") val charSet: String? = null,
    @JsonProperty("EdiType") val ediType: String? = null
)
