package com.vibepay.payment.service.strategy

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.emum.PAY005
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
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class InicisPaymentStrategy(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${payment.inicis.mid}") private val mid: String,
    @Value("\${payment.inicis.api-key}") private val apiKey: String,
    @Value("\${payment.inicis.sign-key}") private val signKey: String,
    @Value("\${payment.inicis.return-url}") private val returnUrl: String,
    @Value("\${payment.inicis.close-url}") private val closeUrl: String,
    @Value("\${payment.inicis.gopaymethod:Card}") private val gopaymethod: String,
    @Value("\${payment.inicis.acceptmethod:below1000}") private val acceptmethod: String
) : PaymentGatewayStrategy {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun getPgType(): PAY005 = PAY005.INICIS

    override fun initiatePayment(request: PaymentInitiateRequest): PaymentInitiateResponse {
        val timestamp = System.currentTimeMillis().toString()
        val mKey = sha256(signKey)
        val formData = mapOf(
            "mid" to mid, "oid" to request.orderNumber, "price" to request.amount.toString(),
            "timestamp" to timestamp, "mKey" to mKey, "version" to "1.0", "currency" to "WON",
            "goodname" to request.productName, "buyername" to request.buyerName,
            "buyertel" to request.buyerTel, "buyeremail" to request.buyerEmail,
            "returnUrl" to returnUrl, "closeUrl" to closeUrl,
            "gopaymethod" to gopaymethod, "acceptmethod" to acceptmethod, "charset" to "UTF-8",
            "signature" to sha256("oid=${request.orderNumber}&price=${request.amount}&timestamp=$timestamp"),
            "verification" to sha256("oid=${request.orderNumber}&price=${request.amount}&signKey=$signKey&timestamp=$timestamp")
        )
        return PaymentInitiateResponse("INICIS", PAY005.INICIS.code, request.paymentMethod, mid, signKey, returnUrl, formData)
    }

    override fun approvePayment(request: PaymentConfirmRequest): PaymentApprovalResult {
        val timestamp = System.currentTimeMillis().toString()
        val params = LinkedMultiValueMap<String, String>().apply {
            add("mid", mid); add("authToken", request.authToken); add("timestamp", timestamp)
            add("signature", sha256("authToken=${request.authToken}&timestamp=$timestamp"))
            add("verification", sha256("authToken=${request.authToken}&signKey=$signKey&timestamp=$timestamp"))
            add("charset", "UTF-8"); add("format", "JSON"); add("price", request.price?.toString())
        }
        val entity = HttpEntity(params, HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED })
        @Suppress("UNCHECKED_CAST")
        val body = restTemplate.postForEntity(request.authUrl!!, entity, Map::class.java).body as? Map<String, Any>
            ?: throw ApiException(ApiError.PAYMENT_APPROVAL_FAILED, "이니시스 승인 응답 없음")
        if (body["resultCode"] != "0000") throw ApiException(ApiError.PAYMENT_APPROVAL_FAILED, "이니시스 승인 실패: ${body["resultMsg"]}")
        return PaymentApprovalResult(body["applNum"] as? String, body["tid"] as? String, (body["totPrice"] as? String)?.toLongOrNull(), body["cardNum"] as? String, body["cardCode"] as? String)
    }

    override fun cancelPayment(payNo: String, orderNo: String, transactionId: String, cancelAmount: Long, cancelableAmount: Long?, originalAmount: Long?, cancelReason: String) {
        val confirmPrice = (cancelableAmount ?: cancelAmount) - cancelAmount
        val isPartial = !(originalAmount == cancelableAmount) || confirmPrice > 0
        val type = if (isPartial) "partialRefund" else "refund"
        val url = if (isPartial) "https://iniapi.inicis.com/v2/pg/partialRefund" else "https://iniapi.inicis.com/v2/pg/refund"
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val data = mutableMapOf<String, Any>("tid" to transactionId, "msg" to cancelReason)
        if (isPartial) { data["price"] = cancelAmount.toString(); data["confirmPrice"] = confirmPrice.toString(); data["currency"] = "WON" }
        val dataJson = objectMapper.writeValueAsString(data)
        val requestBody = mapOf("mid" to mid, "type" to type, "timestamp" to timestamp,
            "clientIp" to "127.0.0.1", "hashData" to sha512(apiKey + mid + type + timestamp + dataJson), "data" to data)
        val entity = HttpEntity(requestBody, HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON })
        @Suppress("UNCHECKED_CAST")
        val body = restTemplate.postForEntity(url, entity, Map::class.java).body as? Map<String, Any>
            ?: throw ApiException(ApiError.PAYMENT_CANCEL_FAILED, "이니시스 취소 응답 없음")
        if (body["resultCode"] != "00") throw ApiException(ApiError.PAYMENT_CANCEL_FAILED, body["resultMsg"] as? String ?: "이니시스 취소 실패")
        log.info("Inicis cancel completed. orderNo={}", orderNo)
    }

    private fun sha256(text: String) = MessageDigest.getInstance("SHA-256").let {
        it.update(text.toByteArray(StandardCharsets.UTF_8)); String.format("%064x", BigInteger(1, it.digest())) }
    private fun sha512(text: String) = MessageDigest.getInstance("SHA-512").let {
        it.update(text.toByteArray(StandardCharsets.UTF_8)); String.format("%0128x", BigInteger(1, it.digest())) }
}
