package com.api.app.dto.response.payment

import com.fasterxml.jackson.annotation.JsonProperty

data class NiceApprovalResponse(
    @JsonProperty("ResultCode") val resultCode: String? = null,
    @JsonProperty("ResultMsg") val resultMsg: String? = null,
    @JsonProperty("AuthCode") val authCode: String? = null,
    @JsonProperty("TID") val tid: String? = null,
    @JsonProperty("AuthDate") val authDate: String? = null,
    @JsonProperty("Amt") val amt: String? = null,
    @JsonProperty("CardNo") val cardNo: String? = null,
    @JsonProperty("CardCode") val cardCode: String? = null,
    @JsonProperty("CardName") val cardName: String? = null,
    @JsonProperty("CardQuota") val cardQuota: String? = null,
    @JsonProperty("CardInterest") val cardInterest: String? = null,
    @JsonProperty("AcquCardCode") val acquCardCode: String? = null,
    @JsonProperty("AcquCardName") val acquCardName: String? = null,
    @JsonProperty("CardCl") val cardCl: String? = null,
    @JsonProperty("CardType") val cardType: String? = null,
    @JsonProperty("PayMethod") val payMethod: String? = null,
    @JsonProperty("MID") val mid: String? = null,
    @JsonProperty("Moid") val moid: String? = null,
    @JsonProperty("BuyerEmail") val buyerEmail: String? = null,
    @JsonProperty("BuyerTel") val buyerTel: String? = null,
    @JsonProperty("BuyerName") val buyerName: String? = null,
    @JsonProperty("GoodsName") val goodsName: String? = null,
    @JsonProperty("MsgSource") val msgSource: String? = null,
    @JsonProperty("Signature") val signature: String? = null,
    @JsonProperty("MallReserved") val mallReserved: String? = null
)
