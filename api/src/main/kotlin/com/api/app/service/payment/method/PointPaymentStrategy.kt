package com.api.app.service.payment.method

import com.api.app.dto.request.order.PayRequest
import com.api.app.dto.request.point.PointTransactionRequest
import com.api.app.emum.MEM002
import com.api.app.emum.MEM003
import com.api.app.emum.PAY001
import com.api.app.emum.PAY002
import com.api.app.emum.PAY003
import com.api.app.emum.PAY004
import com.api.app.entity.PayBase
import com.api.app.entity.PayInterfaceLog
import com.api.app.repository.rwdb.pay.PayBaseTrxRepository
import com.api.app.repository.rwdb.pay.PayInterfaceLogTrxRepository
import com.api.app.service.point.PointService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class PointPaymentStrategy(
    private val pointService: PointService,
    private val payBaseTrxRepository: PayBaseTrxRepository,
    private val payInterfaceLogTrxRepository: PayInterfaceLogTrxRepository,
    private val objectMapper: ObjectMapper
) : PaymentMethodStrategy {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun processPayment(memberNo: String, orderNo: String, payRequest: PayRequest): PayBase {
        log.info("Point payment processing started. memberNo={}, orderNo={}, amount={}", memberNo, orderNo, payRequest.amount)

        val payBase = PayBase().apply {
            this.payTypeCode = PAY001.PAYMENT.code
            this.payWayCode = PAY002.POINT.code
            this.payStatusCode = PAY003.PAYMENT_COMPLETED.code
            this.orderNo = orderNo
            this.memberNo = memberNo
            this.amount = payRequest.amount
            this.cancelableAmount = payRequest.amount
        }
        payBaseTrxRepository.save(payBase)
        val payNo = payBase.payNo

        val pointRequest = PointTransactionRequest(
            amount = payRequest.amount,
            pointTransactionCode = MEM002.USE.code,
            pointTransactionReasonCode = MEM003.ORDER.code,
            pointTransactionReasonNo = payNo
        )

        try {
            val requestJson = objectMapper.writeValueAsString(pointRequest)
            saveInterfaceLog(payNo, memberNo, PAY004.PAYMENT.code, requestJson, null)
        } catch (e: Exception) {
            log.error("Failed to log point payment request. payNo={}", payNo, e)
        }

        pointService.processPointTransaction(memberNo, pointRequest)

        try {
            val responseJson = """{"payNo":"$payNo","amount":${payRequest.amount},"status":"completed"}"""
            saveInterfaceLog(payNo, memberNo, PAY004.APPROVAL.code, null, responseJson)
        } catch (e: Exception) {
            log.error("Failed to log point payment approval. payNo={}", payNo, e)
        }

        payBase.payFinishDateTime = LocalDateTime.now()

        log.info("Point payment completed. memberNo={}, orderNo={}, payNo={}", memberNo, orderNo, payNo)
        return payBase
    }

    override fun getPayWayCode(): String = PAY002.POINT.code

    private fun saveInterfaceLog(payNo: String, memberNo: String, payLogCode: String,
                                  requestJson: String?, responseJson: String?) {
        try {
            val interfaceLog = PayInterfaceLog().apply {
                this.memberNo = memberNo
                this.payNo = payNo
                this.payLogCode = payLogCode
                this.requestJson = requestJson
                this.responseJson = responseJson
            }
            payInterfaceLogTrxRepository.save(interfaceLog)
        } catch (e: Exception) {
            log.error("Error creating pay_interface_log. payNo={}", payNo, e)
        }
    }
}
