package com.api.app.service.payment.strategy

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.dto.request.payment.NiceApprovalRequest
import com.api.app.dto.request.payment.PaymentCancelRequest
import com.api.app.dto.request.payment.PaymentConfirmRequest
import com.api.app.dto.request.payment.PaymentInitiateRequest
import com.api.app.dto.response.payment.NiceApprovalResponse
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
class NicePaymentStrategy(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${payment.nice.mid}") private val mid: String,
    @Value("\${payment.nice.merchant-key}") private val merchantKey: String,
    @Value("\${payment.nice.return-url}") private val returnUrl: String,
    @Value("\${payment.nice.cancel-url}") private val cancelUrl: String
) : PaymentGatewayStrategy {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun initiatePayment(request: PaymentInitiateRequest): PaymentInitiateResponse {
        val ediDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val signData = sha256Hash(ediDate + mid + request.amount + merchantKey)

        val formData = mapOf(
            "PayMethod" to "CARD",
            "MID" to mid,
            "Moid" to request.orderNumber,
            "Amt" to request.amount.toString(),
            "GoodsName" to request.productName,
            "BuyerName" to request.buyerName,
            "BuyerEmail" to request.buyerEmail,
            "BuyerTel" to request.buyerTel,
            "ReturnURL" to returnUrl,
            "EdiDate" to ediDate,
            "CharSet" to "UTF-8",
            "SignData" to signData
        )

        log.info("Nice payment initiate completed. orderNumber={}", request.orderNumber)

        return PaymentInitiateResponse(
            pgType = "NICE",
            pgTypeCode = PAY005.NICE.code,
            paymentMethod = request.paymentMethod,
            merchantId = mid,
            merchantKey = merchantKey,
            returnUrl = returnUrl,
            formData = formData
        )
    }

    override fun approvePayment(request: PaymentConfirmRequest): PaymentApprovalResponse {
        log.info("Nice payment approval started. orderNo={}", request.orderNo)

        val ediDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val signData = sha256Hash("${request.authToken}${request.mid}${request.amount}$ediDate$merchantKey")

        val niceRequest = NiceApprovalRequest(
            tid = request.tradeNo,
            authToken = request.authToken,
            mid = request.mid,
            amt = request.amount,
            ediDate = ediDate,
            signData = signData,
            charSet = "UTF-8",
            ediType = "JSON"
        )

        val params = convertToMultiValueMap(niceRequest)
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }
        val entity = HttpEntity(params, headers)

        val response = restTemplate.postForEntity(request.authUrl!!, entity, String::class.java)
        val responseBodyStr = response.body?.takeIf { it.isNotEmpty() }
            ?: throw ApiException(ApiError.PAYMENT_APPROVAL_FAILED, "나이스 승인 응답이 없습니다")

        val responseBody = try {
            objectMapper.readValue(responseBodyStr, NiceApprovalResponse::class.java)
        } catch (e: Exception) {
            log.error("Failed to parse Nice approval response: {}", responseBodyStr, e)
            throw ApiException(ApiError.PAYMENT_APPROVAL_FAILED, "나이스 응답 파싱 실패: ${e.message}")
        }

        if (responseBody.resultCode != "3001") {
            log.error("Nice approval failed. ResultCode={}, ResultMsg={}", responseBody.resultCode, responseBody.resultMsg)
            throw ApiException(ApiError.PAYMENT_APPROVAL_FAILED, "나이스 결제 승인 실패: ${responseBody.resultMsg}")
        }

        return PaymentApprovalResponse(
            approveNo = responseBody.authCode,
            trdNo = responseBody.tid,
            amount = responseBody.amt?.toLongOrNull(),
            cardNo = responseBody.cardNo,
            cardCode = responseBody.cardCode
        )
    }

    override fun cancelPayment(request: PaymentConfirmRequest) {
        log.warn("Nice payment cancellation not implemented yet. TODO: call PG netCancel API")
    }

    override fun cancelPaymentByOrder(request: PaymentCancelRequest) {
        log.info("Nice order cancel started. orderNo={}, tid={}, cancelAmount={}", request.orderNo, request.transactionId, request.cancelAmount)

        val cancelableAmount = request.cancelableAmount
            ?: throw ApiException(ApiError.INVALID_PARAMETER, "취소 가능한 금액 정보가 필요합니다")
        val originalAmount = request.originalAmount
            ?: throw ApiException(ApiError.INVALID_PARAMETER, "원 승인 금액 정보가 필요합니다")

        val remainingAmount = cancelableAmount - request.cancelAmount
        val isPartialCancel = !originalAmount.equals(cancelableAmount) || remainingAmount > 0
        val partialCancelCode = if (isPartialCancel) "1" else "0"

        val url = "https://pg-api.nicepay.co.kr/webapi/cancel_process.jsp"
        val ediDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val signData = sha256Hash("$mid${request.cancelAmount}$ediDate$merchantKey")

        val params = LinkedMultiValueMap<String, String>().apply {
            add("TID", request.transactionId)
            add("MID", mid)
            add("Moid", request.orderNo)
            add("CancelAmt", request.cancelAmount.toString())
            add("CancelMsg", request.cancelReason)

            add("PartialCancelCode", partialCancelCode)
            add("EdiDate", ediDate)
            add("SignData", signData)
            add("CharSet", "utf-8")
            add("EdiType", "JSON")
        }

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }
        val entity = HttpEntity(params, headers)

        val response = restTemplate.postForEntity(url, entity, String::class.java)
        val responseBodyStr = response.body?.takeIf { it.isNotEmpty() }
            ?: throw ApiException(ApiError.PAYMENT_CANCEL_FAILED, "나이스 취소 응답이 없습니다")

        @Suppress("UNCHECKED_CAST")
        val responseBody = try {
            objectMapper.readValue(responseBodyStr, Map::class.java) as Map<String, Any>
        } catch (e: Exception) {
            log.error("Failed to parse Nice cancel response: {}", responseBodyStr, e)
            throw ApiException(ApiError.PAYMENT_CANCEL_FAILED, "나이스 취소 응답 파싱 실패: ${e.message}")
        }

        val resultCode = responseBody["ResultCode"] as? String
        if (resultCode != "2001") {
            val resultMsg = responseBody["ResultMsg"] as? String
            log.error("Nice cancel failed. orderNo={}, resultCode={}, resultMsg={}", request.orderNo, resultCode, resultMsg)
            throw ApiException(ApiError.PAYMENT_CANCEL_FAILED, resultMsg ?: "나이스 취소 실패")
        }

        log.info("Nice order cancel completed. orderNo={}", request.orderNo)
    }

    override fun getPgType(): PAY005 = PAY005.NICE

    private fun sha256Hash(text: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(text.toByteArray(StandardCharsets.UTF_8))
        return String.format("%064x", BigInteger(1, md.digest()))
    }

    private fun convertToMultiValueMap(dto: Any): MultiValueMap<String, String> {
        val params = LinkedMultiValueMap<String, String>()
        @Suppress("UNCHECKED_CAST")
        val map = objectMapper.convertValue(dto, Map::class.java) as Map<String, Any?>
        map.forEach { (key, value) -> if (value != null) params.add(key, value.toString()) }
        return params
    }
}
