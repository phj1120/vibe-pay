package com.vibe.pay.backend.payment;

import com.vibe.pay.backend.enums.PgCompany;
import com.vibe.pay.backend.exception.PaymentException;
import com.vibe.pay.backend.payment.factory.PaymentGatewayFactory;
import com.vibe.pay.backend.payment.factory.PaymentProcessorFactory;
import com.vibe.pay.backend.payment.gateway.PaymentGatewayAdapter;
import com.vibe.pay.backend.payment.dto.PaymentInitResponse;
import com.vibe.pay.backend.payment.processor.PaymentProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentMapper paymentMapper;
    private final PaymentProcessorFactory paymentProcessorFactory;
    private final PaymentGatewayFactory paymentGatewayFactory;
    private final PgWeightSelector pgWeightSelector;


    public Optional<Payment> getPaymentById(String paymentId) {
        return Optional.ofNullable(paymentMapper.findByPaymentId(paymentId));
    }

    public List<Payment> findByOrderId(String orderId) {
        return paymentMapper.findByOrderId(orderId);
    }

    public List<Payment> getAllPayments() {
        return paymentMapper.findAll();
    }

    public Payment updatePayment(String paymentId, Payment paymentDetails) {
        Payment existingPayment = paymentMapper.findByPaymentId(paymentId);
        if (existingPayment == null) {
            throw PaymentException.approvalFailed("Payment not found with id " + paymentId);
        }
        paymentDetails.setPaymentId(paymentId);
        paymentMapper.update(paymentDetails);
        return paymentDetails;
    }

    public void deletePayment(String paymentId) {
        Payment payment = paymentMapper.findByPaymentId(paymentId);
        if (payment == null) {
            throw PaymentException.approvalFailed("Payment not found with id " + paymentId);
        }
        paymentMapper.delete(payment);
    }

    @Transactional
    public PaymentInitResponse initiatePayment(PaymentInitiateRequest request) {
        log.info("Initiating payment with Factory/Adapter pattern: orderId={}, method={}, pgCompany={}",
                request.getOrderId(), request.getPaymentMethod(), request.getPgCompany());

        try {
            // 1. PG 어댑터 선택 (가중치 기반 또는 직접 선택)
            String pgCompany = determinePgCompany(request.getPgCompany());
            PaymentGatewayAdapter pgAdapter = paymentGatewayFactory.getAdapter(pgCompany);

            // 2. PG 결제 시작 처리
            PaymentInitResponse pgResponse = pgAdapter.initiate(request);

            if (!pgResponse.isSuccess()) {
                throw PaymentException.initiationFailed(pgResponse.getErrorMessage());
            }

            // 3. 실제 선택된 PG사 정보를 응답에 설정
            pgResponse.setSelectedPgCompany(pgCompany);

            log.info("Payment initiation completed: orderId={}, paymentId={}, selectedPgCompany={}",
                    request.getOrderId(), pgResponse.getPaymentId(), pgCompany);

            return pgResponse;

        } catch (PaymentException e) {
            log.error("Payment initiation failed: {}", e.getErrorMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during payment initiation: {}", e.getMessage(), e);
            throw PaymentException.pgSystemError("PAYMENT_SYSTEM", e);
        }
    }

    /**
     * PG사를 결정합니다. WEIGHTED 옵션인 경우 가중치 기반으로 선택합니다.
     */
    private String determinePgCompany(String requestedPgCompany) {
        if ("WEIGHTED".equals(requestedPgCompany)) {
            String selectedPg = pgWeightSelector.selectPgCompanyByWeight();
            log.info("Weight-based PG selection: {} (weights: {})", 
                    selectedPg, pgWeightSelector.getWeightInfo());
            return selectedPg;
        }
        
        return requestedPgCompany != null ? requestedPgCompany : "INICIS";
    }

    @Transactional
    public Payment confirmPayment(PaymentConfirmRequest request) {
        log.info("Processing payment confirmation with Factory/Adapter pattern: orderId={}, method={}",
                request.getOrderId(), request.getPaymentMethod());

        try {
            // 1. 결제수단별 Processor 선택 및 처리 (PG 승인 포함)
            PaymentProcessor processor = paymentProcessorFactory.getProcessor(request.getPaymentMethod());

            return processor.processPayment(request);
        } catch (PaymentException e) {
            log.error("Payment confirmation failed: {}", e.getErrorMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during payment confirmation: {}", e.getMessage(), e);
            throw PaymentException.pgSystemError("PAYMENT_SYSTEM", e);
        }
    }
}