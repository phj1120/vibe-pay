package com.vibe.pay.backend.payment;

import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLog;
import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLogMapper;
import com.vibe.pay.backend.order.Order;
import com.vibe.pay.backend.order.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.security.MessageDigest;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.math.BigDecimal;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private PaymentInterfaceRequestLogMapper paymentInterfaceRequestLogMapper;

    @Autowired
    private OrderMapper orderMapper;

    // 환경별 설정 주입
    @Value("${inicis.mid}")
    private String inicisMid;

    @Value("${inicis.signKey}")
    private String inicisSignKey;

    @Value("${inicis.returnUrl}")
    private String inicisReturnUrl;

    @Value("${inicis.closeUrl}")
    private String inicisCloseUrl;

    @Value("${inicis.currency:WON}")
    private String inicisCurrency;

    @Value("${inicis.version:1.0}")
    private String inicisVersion;

    @Value("${inicis.gopaymethod:Card}")
    private String inicisGopaymethod;

    @Value("${inicis.acceptmethod:below1000}")
    private String inicisAcceptmethod;

    public Payment createPayment(Payment payment) {
        paymentMapper.insert(payment);
        return payment;
    }

    public Optional<Payment> getPaymentById(Long id) {
        return Optional.ofNullable(paymentMapper.findById(id));
    }

    public List<Payment> getAllPayments() {
        return paymentMapper.findAll();
    }

    public Payment updatePayment(Long id, Payment paymentDetails) {
        Payment existingPayment = paymentMapper.findById(id);
        if (existingPayment == null) {
            throw new RuntimeException("Payment not found with id " + id);
        }
        paymentDetails.setId(id);
        paymentMapper.update(paymentDetails);
        return paymentDetails;
    }

    public void deletePayment(Long id) {
        paymentMapper.delete(id);
    }

    @Transactional
    public InicisPaymentParameters initiatePayment(PaymentInitiateRequest request) {
        // 결제 시작 로그
        log.info("Initiating payment: memberId={}, amount={}, method={}", request.getMemberId(), request.getAmount(), request.getPaymentMethod());

        // PG 선택(샘플)
        Random random = new Random();
        String pgCompany = random.nextBoolean() ? "INICIS" : "NICEPAY";
        log.debug("Selected PG company: {}", pgCompany);

        // 결제 레코드 생성(주문은 결제 성공 후 생성/연결)
        Payment payment = new Payment(
                request.getMemberId(),
                request.getAmount(),
                request.getPaymentMethod(),
                pgCompany,
                "INITIATED",
                null
        );
        paymentMapper.insert(payment);

        // 이니시스 파라미터 구성 - 화면에서 넘어온 값 사용
        InicisPaymentParameters inicisParams = new InicisPaymentParameters();
        inicisParams.setMid(inicisMid);

        // 주문이 아직 없으므로 서버에서 OID 생성(결제ID 기반)
        String oid = "OID-" + payment.getId() + "-" + System.currentTimeMillis();
        inicisParams.setOid(oid);

        // price: 요청 금액을 정수 문자열로 변환
        String priceStr = "0";
        if (request.getAmount() != null) {
            BigDecimal amt = BigDecimal.valueOf(request.getAmount()).setScale(0, RoundingMode.HALF_UP);
            priceStr = amt.toPlainString();
        }
        inicisParams.setPrice(priceStr);

        // 화면에서 넘어온 표기 정보
        inicisParams.setGoodName(request.getGoodName() != null ? request.getGoodName() : "주문결제");
        inicisParams.setBuyerName(request.getBuyerName());
        inicisParams.setBuyerTel(request.getBuyerTel());
        inicisParams.setBuyerEmail(request.getBuyerEmail());

        // 환경설정 기반 옵션
        inicisParams.setVersion(inicisVersion);
        inicisParams.setCurrency(inicisCurrency);
        inicisParams.setMoId(String.valueOf(payment.getId()));
        inicisParams.setGopaymethod(inicisGopaymethod);
        inicisParams.setAcceptmethod(inicisAcceptmethod);

        // 타임스탬프(ms)
        String timestamp = String.valueOf(System.currentTimeMillis());
        inicisParams.setTimestamp(timestamp);

        // signKey 정제
        final String finalSignKey = (inicisSignKey != null) ? inicisSignKey.trim() : null;
        if (finalSignKey == null || finalSignKey.isBlank()) {
            log.error("Inicis signKey is not configured. Set INICIS_SIGN_KEY env/property for MID={}", inicisMid);
            throw new RuntimeException("INICIS signKey not configured");
        }

        // 단일 SHA-256 메서드로 mKey/Signature/Verification 생성
        inicisParams.setmKey(sha256Hex(finalSignKey));

        String signingText = "oid=" + inicisParams.getOid()
                + "&price=" + inicisParams.getPrice()
                + "&timestamp=" + inicisParams.getTimestamp();
        inicisParams.setSignature(sha256Hex(signingText));

        String verificationText = "oid=" + inicisMid
                + "&oid=" + inicisParams.getOid()
                + "&price=" + inicisParams.getPrice()
                + "&signKey=" + finalSignKey
                + "&timestamp=" + inicisParams.getTimestamp();
        inicisParams.setVerification(sha256Hex(verificationText));

        // 환경설정 기반 콜백 URL
        inicisParams.setReturnUrl(inicisReturnUrl);
        inicisParams.setCloseUrl(inicisCloseUrl);

        // 인터페이스 로그 적재
        PaymentInterfaceRequestLog logRow = new PaymentInterfaceRequestLog(
                payment.getId(),
                "INITIATE_PAYMENT",
                "Request Payload: memberId=" + request.getMemberId() + ", amount=" + request.getAmount(),
                "Inicis Parameters issued (oid=" + oid + ", price=" + priceStr + ")"
        );
        paymentInterfaceRequestLogMapper.insert(logRow);

        log.info("Payment initiation parameters issued for paymentId={}, oid={}", payment.getId(), oid);
        return inicisParams;
    }

    @Transactional
    public Payment confirmPayment(PaymentConfirmRequest request) {
        Payment payment = paymentMapper.findById(request.getPaymentId());
        if (payment == null) {
            throw new RuntimeException("Payment not found with id " + request.getPaymentId());
        }

        // Update payment status and transaction ID
        payment.setStatus(request.getStatus());
        payment.setTransactionId(request.getTransactionId());
        paymentMapper.update(payment);

        // Update associated Order status
        Order order = orderMapper.findByPaymentId(payment.getId());
        if (order == null) {
            throw new RuntimeException("Order not found for payment ID: " + payment.getId());
        }

        if ("SUCCESS".equals(request.getStatus())) {
            order.setStatus("COMPLETED");
        } else if ("FAILURE".equals(request.getStatus())) {
            order.setStatus("FAILED");
        }
        orderMapper.update(order);


        // Create PaymentInterfaceRequestLog entry for confirmation
        PaymentInterfaceRequestLog log = new PaymentInterfaceRequestLog(
                payment.getId(),
                "CONFIRM_PAYMENT",
                "Request Payload: " + request.toString(), // Simplified payload
                "Confirmation Status: " + request.getStatus() + ", Transaction ID: " + request.getTransactionId() // Simplified response
        );
        paymentInterfaceRequestLogMapper.insert(log);

        return payment;
    }

    @Transactional
    public Payment processPayment(Payment payment) {
        // This is a placeholder for actual PG integration logic
        // In a real scenario, you would call a PG API here
        System.out.println("Processing payment for amount: " + payment.getAmount() + " with PG: " + payment.getPgCompany());
        payment.setStatus("APPROVED"); // Simulate approval
        paymentMapper.update(payment);
        return payment;
    }

    @Transactional
    public Payment cancelPayment(Long paymentId) {
        Payment payment = paymentMapper.findById(paymentId);
        if (payment == null) {
            throw new RuntimeException("Payment not found with id " + paymentId);
        }

        if ("CANCELLED".equals(payment.getStatus())) {
            throw new RuntimeException("Payment is already cancelled.");
        }

        // Simulate PG cancellation
        System.out.println("Simulating PG cancellation for payment ID: " + paymentId + " with PG: " + payment.getPgCompany());

        // Update payment status
        payment.setStatus("CANCELLED");
        payment.setTransactionId(payment.getTransactionId() + "-CANCEL"); // Append -CANCEL for simulation
        paymentMapper.update(payment);

        // Create PaymentInterfaceRequestLog entry for cancellation
        PaymentInterfaceRequestLog log = new PaymentInterfaceRequestLog(
                payment.getId(),
                "CANCEL_PAYMENT",
                "Request Payload: Payment ID " + paymentId, // Simplified payload
                "Cancellation Status: SUCCESS, Transaction ID: " + payment.getTransactionId() // Simplified response
        );
        paymentInterfaceRequestLogMapper.insert(log);

        // In a real scenario, you might also update the associated Order status here
        // or handle it in OrderService.cancelOrder
        // For now, OrderService.cancelOrder will handle the order status update.

        return payment;
    }

    private String generateOrdNo() {
        return "ORD-" + System.currentTimeMillis();
    }

    private static String sha256Hex(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes(StandardCharsets.UTF_8));
            return String.format("%064x", new BigInteger(1, md.digest()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute SHA-256", e);
        }
    }
}