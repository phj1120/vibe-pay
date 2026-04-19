package com.api.app.service.payment.method

import com.api.app.dto.request.order.PayRequest
import com.api.app.emum.PAY001
import com.api.app.emum.PAY002
import com.api.app.emum.PAY003
import com.api.app.emum.PAY004
import com.api.app.entity.PayBase
import com.api.app.entity.PayInterfaceLog
import com.api.app.repository.rwdb.pay.PayBaseTrxRepository
import com.api.app.repository.rwdb.pay.PayInterfaceLogTrxRepository
import com.api.app.service.payment.strategy.PaymentGatewayFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CardPaymentStrategy(
    private val paymentGatewayFactory: PaymentGatewayFactory,
    private val payBaseTrxRepository: PayBaseTrxRepository,
    private val payInterfaceLogTrxRepository: PayInterfaceLogTrxRepository,
    private val objectMapper: ObjectMapper
) : PaymentMethodStrategy {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun processPayment(memberNo: String, orderNo: String, payRequest: PayRequest): PayBase {
        log.info("Card payment processing started. orderNo={}, amount={}", orderNo, payRequest.amount)

        val confirmRequest = payRequest.paymentConfirmRequest!!
        val pgType = com.api.app.emum.PAY005.findByCode(confirmRequest.pgTypeCode!!)!!
        val pgStrategy = paymentGatewayFactory.getStrategy(pgType)

        val payBase = PayBase().apply {
            this.payTypeCode = PAY001.PAYMENT.code
            this.payWayCode = PAY002.CREDIT_CARD.code
            this.payStatusCode = PAY003.PAYMENT_COMPLETED.code
            this.orderNo = orderNo
            this.memberNo = memberNo
            this.amount = payRequest.amount
            this.cancelableAmount = payRequest.amount
            this.pgTypeCode = confirmRequest.pgTypeCode
        }
        payBaseTrxRepository.save(payBase)
        val payNo = payBase.payNo

        try {
            val requestJson = objectMapper.writeValueAsString(confirmRequest)
            saveInterfaceLog(payNo, memberNo, PAY004.PAYMENT.code, requestJson, null)
        } catch (e: Exception) {
            log.error("Failed to log payment request. payNo={}", payNo, e)
        }

        val approvalResponse = pgStrategy.approvePayment(confirmRequest)

        try {
            val responseJson = objectMapper.writeValueAsString(approvalResponse)
            saveInterfaceLog(payNo, memberNo, PAY004.APPROVAL.code, null, responseJson)
        } catch (e: Exception) {
            log.error("Failed to log payment approval. payNo={}", payNo, e)
        }

        payBase.approveNo = approvalResponse.approveNo
        payBase.trdNo = approvalResponse.trdNo
        payBase.payFinishDateTime = LocalDateTime.now()

        log.info("Card payment completed. orderNo={}, payNo={}", orderNo, payNo)
        return payBase
    }

    override fun getPayWayCode(): String = PAY002.CREDIT_CARD.code

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
            this.log.error("Error creating pay_interface_log. payNo={}", payNo, e)
        }
    }
}
