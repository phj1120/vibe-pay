package com.api.app.service.payment.strategy

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.dto.request.payment.InicisApprovalRequest
import com.api.app.dto.request.payment.PaymentCancelRequest
import com.api.app.dto.request.payment.PaymentConfirmRequest
import com.api.app.dto.request.payment.PaymentInitiateRequest
import com.api.app.dto.response.payment.InicisApprovalResponse
import com.api.app.dto.response.payment.PaymentApprovalResponse
import com.api.app.dto.response.payment.PaymentInitiateResponse
import com.api.app.emum.PAY005
import com.fasterxml.jackson.databind.ObjectMapper
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

    override fun initiatePayment(request: PaymentInitiateRequest): PaymentInitiateResponse {
        val timestamp = System.currentTimeMillis().toString()
        val mKey = sha256Hash(signKey)

        val signatureData = "oid=${request.orderNumber}&price=${request.amount}&timestamp=$timestamp"
        val verificationData = "oid=${request.orderNumber}&price=${request.amount}&signKey=$signKey&timestamp=$timestamp"

        val formData = mapOf(
            "mid" to mid,
            "oid" to request.orderNumber,
            "price" to request.amount.toString(),
            "timestamp" to timestamp,
            "mKey" to mKey,
            "version" to "1.0",
            "currency" to "WON",
            "goodname" to request.productName,
            "buyername" to request.buyerName,
            "buyertel" to request.buyerTel,
            "buyeremail" to request.buyerEmail,
            "returnUrl" to returnUrl,
            "closeUrl" to closeUrl,
            "gopaymethod" to gopaymethod,
            "acceptmethod" to acceptmethod,
            "charset" to "UTF-8",
            "use_chkfake" to "",
            "signature" to sha256Hash(signatureData),
            "verification" to sha256Hash(verificationData)
        )

        log.info("Inicis payment initiate completed. orderNumber={}", request.orderNumber)

        return PaymentInitiateResponse(
            pgType = "INICIS",
            pgTypeCode = PAY005.INICIS.code,
            paymentMethod = request.paymentMethod,
            merchantId = mid,
            merchantKey = signKey,
            returnUrl = returnUrl,
            formData = formData
        )
    }

    override fun approvePayment(request: PaymentConfirmRequest): PaymentApprovalResponse {
        log.info("Inicis payment approval started. orderNo={}", request.orderNo)

        val timestamp = System.currentTimeMillis().toString()
        val signatureData = "authToken=${request.authToken}&timestamp=$timestamp"
        val verificationData = "authToken=${request.authToken}&signKey=$signKey&timestamp=$timestamp"

        val inicisRequest = InicisApprovalRequest(
            mid = mid,
            authToken = request.authToken,
            timestamp = timestamp,
            signature = sha256Hash(signatureData),
            verification = sha256Hash(verificationData),
            charset = "UTF-8",
            format = "JSON",
            price = request.price
        )

        val params = convertToMultiValueMap(inicisRequest)
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }
        val entity = HttpEntity(params, headers)

        val response = restTemplate.postForEntity(request.authUrl!!, entity, InicisApprovalResponse::class.java)
        val body = response.body ?: throw ApiException(ApiError.PAYMENT_APPROVAL_FAILED, "이니시스 승인 응답이 없습니다")

        if (body.resultCode != "0000") {
            log.error("Inicis approval failed. resultCode={}, resultMsg={}", body.resultCode, body.resultMsg)
            throw ApiException(ApiError.PAYMENT_APPROVAL_FAILED, "이니시스 결제 승인 실패: ${body.resultMsg}")
        }

        return PaymentApprovalResponse(
            approveNo = body.applNum,
            trdNo = body.tid,
            amount = body.totPrice?.toLongOrNull(),
            cardNo = body.cardNum,
            cardCode = body.cardCode
        )
    }

    override fun cancelPayment(request: PaymentConfirmRequest) {
        log.warn("Inicis payment cancellation not implemented yet. TODO: call PG netCancel API")
    }

    override fun cancelPaymentByOrder(request: PaymentCancelRequest) {
        log.info("Inicis order cancel started. orderNo={}, tid={}, cancelAmount={}", request.orderNo, request.transactionId, request.cancelAmount)

        val cancelableAmount = request.cancelableAmount
            ?: throw ApiException(ApiError.INVALID_PARAMETER, "취소 가능한 금액 정보가 필요합니다")
        val originalAmount = request.originalAmount
            ?: throw ApiException(ApiError.INVALID_PARAMETER, "원 승인 금액 정보가 필요합니다")

        val confirmPrice = cancelableAmount - request.cancelAmount
        val isPartialCancel = !originalAmount.equals(cancelableAmount) || confirmPrice > 0
        val type = if (isPartialCancel) "partialRefund" else "refund"
        val url = if (isPartialCancel) "https://iniapi.inicis.com/v2/pg/partialRefund" else "https://iniapi.inicis.com/v2/pg/refund"

        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val data = mutableMapOf("tid" to request.transactionId, "msg" to request.cancelReason)
        if (isPartialCancel) {
            data["price"] = request.cancelAmount.toString()
            data["confirmPrice"] = confirmPrice.toString()
            data["currency"] = "WON"
        }

        val dataJson = objectMapper.writeValueAsString(data)
        val hashData = sha512Hash(apiKey + mid + type + timestamp + dataJson)

        val requestBody = mapOf(
            "mid" to mid,
            "type" to type,
            "timestamp" to timestamp,
            "clientIp" to "127.0.0.1",
            "hashData" to hashData,
            "data" to data
        )

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(requestBody, headers)

        @Suppress("UNCHECKED_CAST")
        val response = restTemplate.postForEntity(url, entity, Map::class.java)
        val responseBody = response.body as? Map<String, Any>
            ?: throw ApiException(ApiError.PAYMENT_CANCEL_FAILED, "이니시스 취소 응답이 없습니다")

        val resultCode = responseBody["resultCode"] as? String
        if (resultCode != "00") {
            val resultMsg = responseBody["resultMsg"] as? String
            log.error("Inicis cancel failed. orderNo={}, resultCode={}, resultMsg={}", request.orderNo, resultCode, resultMsg)
            throw ApiException(ApiError.PAYMENT_CANCEL_FAILED, resultMsg ?: "이니시스 취소 실패")
        }

        log.info("Inicis order cancel completed. orderNo={}, type={}", request.orderNo, type)
    }

    override fun getPgType(): PAY005 = PAY005.INICIS

    private fun sha256Hash(text: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(text.toByteArray(StandardCharsets.UTF_8))
        return String.format("%064x", BigInteger(1, md.digest()))
    }

    private fun sha512Hash(text: String): String {
        val md = MessageDigest.getInstance("SHA-512")
        md.update(text.toByteArray(StandardCharsets.UTF_8))
        return String.format("%0128x", BigInteger(1, md.digest()))
    }

    private fun convertToMultiValueMap(dto: Any): MultiValueMap<String, String> {
        val params = LinkedMultiValueMap<String, String>()
        @Suppress("UNCHECKED_CAST")
        val map = objectMapper.convertValue(dto, Map::class.java) as Map<String, Any?>
        map.forEach { (key, value) -> if (value != null) params.add(key, value.toString()) }
        return params
    }
}
