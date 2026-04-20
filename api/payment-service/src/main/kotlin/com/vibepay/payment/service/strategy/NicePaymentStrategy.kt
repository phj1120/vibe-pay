package com.vibepay.payment.service.strategy

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.emum.PAY005
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.vibepay.payment.dto.PaymentConfirmRequest
import com.vibepay.payment.dto.PaymentInitiateRequest
import com.vibepay.payment.dto.PaymentInitiateResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class NicePaymentStrategy(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${payment.nice.mid}") private val mid: String,
    @Value("\${payment.nice.merchant-key}") private val merchantKey: String,
    @Value("\${payment.nice.return-url}") private val returnUrl: String,
    @Value("\${payment.nice.cancel-url}") private val cancelUrl: String
) : PaymentGatewayStrategy {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun getPgType(): PAY005 = PAY005.NICE

    override fun initiatePayment(request: PaymentInitiateRequest): PaymentInitiateResponse {
        val ediDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val formData = mapOf(
            "PayMethod" to "CARD", "MID" to mid, "Moid" to request.orderNumber,
            "Amt" to request.amount.toString(), "GoodsName" to request.productName,
            "BuyerName" to request.buyerName, "BuyerEmail" to request.buyerEmail,
            "BuyerTel" to request.buyerTel, "ReturnURL" to returnUrl,
            "EdiDate" to ediDate, "CharSet" to "UTF-8",
            "SignData" to sha256(ediDate + mid + request.amount + merchantKey)
        )
        return PaymentInitiateResponse("NICE", PAY005.NICE.code, request.paymentMethod, mid, merchantKey, returnUrl, formData)
    }

    override fun approvePayment(request: PaymentConfirmRequest): PaymentApprovalResult {
        val ediDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val params = LinkedMultiValueMap<String, String>().apply {
            add("TID", request.transactionId); add("AuthToken", request.authToken)
            add("MID", request.mid); add("Amt", request.niceAmount)
            add("EdiDate", ediDate); add("SignData", sha256("${request.authToken}${request.mid}${request.niceAmount}$ediDate$merchantKey"))
            add("CharSet", "UTF-8"); add("EdiType", "JSON")
        }
        val entity = HttpEntity(params, HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED })
        val body = restTemplate.postForEntity(request.authUrl!!, entity, String::class.java).body
            ?: throw ApiException(ApiError.PAYMENT_APPROVAL_FAILED, "나이스 승인 응답 없음")
        @Suppress("UNCHECKED_CAST")
        val map = objectMapper.readValue(body, Map::class.java) as Map<String, Any>
        if (map["ResultCode"] != "3001") throw ApiException(ApiError.PAYMENT_APPROVAL_FAILED, "나이스 승인 실패: ${map["ResultMsg"]}")
        return PaymentApprovalResult(map["AuthCode"] as? String, map["TID"] as? String, (map["Amt"] as? String)?.toLongOrNull(), map["CardNo"] as? String, map["CardCode"] as? String)
    }

    override fun cancelPayment(payNo: String, orderNo: String, transactionId: String, cancelAmount: Long, cancelableAmount: Long?, originalAmount: Long?, cancelReason: String) {
        val remaining = (cancelableAmount ?: cancelAmount) - cancelAmount
        val isPartial = !(originalAmount == cancelableAmount) || remaining > 0
        val ediDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val params = LinkedMultiValueMap<String, String>().apply {
            add("TID", transactionId); add("MID", mid); add("Moid", orderNo)
            add("CancelAmt", cancelAmount.toString()); add("CancelMsg", cancelReason)
            add("PartialCancelCode", if (isPartial) "1" else "0")
            add("EdiDate", ediDate); add("SignData", sha256("$mid$cancelAmount$ediDate$merchantKey"))
            add("CharSet", "utf-8"); add("EdiType", "JSON")
        }
        val entity = HttpEntity(params, HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED })
        val body = restTemplate.postForEntity("https://pg-api.nicepay.co.kr/webapi/cancel_process.jsp", entity, String::class.java).body
            ?: throw ApiException(ApiError.PAYMENT_CANCEL_FAILED, "나이스 취소 응답 없음")
        @Suppress("UNCHECKED_CAST")
        val map = objectMapper.readValue(body, Map::class.java) as Map<String, Any>
        if (map["ResultCode"] != "2001") throw ApiException(ApiError.PAYMENT_CANCEL_FAILED, map["ResultMsg"] as? String ?: "나이스 취소 실패")
        log.info("Nice cancel completed. orderNo={}", orderNo)
    }

    private fun sha256(text: String) = MessageDigest.getInstance("SHA-256").let {
        it.update(text.toByteArray(StandardCharsets.UTF_8)); String.format("%064x", BigInteger(1, it.digest())) }
}
