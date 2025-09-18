package com.vibe.pay.backend.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        Payment createdPayment = paymentService.createPayment(payment);
        // Optionally, trigger processPayment here or in a separate endpoint
        // paymentService.processPayment(createdPayment);
        return ResponseEntity.ok(createdPayment);
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable String id) {
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable String id, @RequestBody Payment paymentDetails) {
        try {
            Payment updatedPayment = paymentService.updatePayment(id, paymentDetails);
            return ResponseEntity.ok(updatedPayment);
        } catch (RuntimeException e) {
            log.error("updatePayment failed for id={}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable String id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/initiate")
    public ResponseEntity<InicisPaymentParameters> initiatePayment(@RequestBody PaymentInitiateRequest request) { // Changed return type
        log.info("Received payment initiate request: memberId={}, amount={}, method={}",
                request.getMemberId(), request.getAmount(), request.getPaymentMethod());
        try {
            InicisPaymentParameters inicisParams = paymentService.initiatePayment(request);
            log.info("Payment initiation successful for memberId={}", request.getMemberId());
            return ResponseEntity.ok(inicisParams);
        } catch (Exception e) {
            log.error("initiatePayment failed. request={}", request, e);
            log.error("Error details: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null); // Return null or specific error DTO
        }
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<Payment> processPayment(@PathVariable String id) {
        try {
            Payment payment = paymentService.getPaymentById(id)
                    .orElseThrow(() -> new RuntimeException("Payment not found with id " + id));
            Payment processedPayment = paymentService.processPayment(payment);
            return ResponseEntity.ok(processedPayment);
        } catch (RuntimeException e) {
            log.error("processPayment failed for id={}", id, e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Payment> cancelPayment(@PathVariable String id) {
        try {
            Payment cancelledPayment = paymentService.cancelPayment(id);
            return ResponseEntity.ok(cancelledPayment);
        } catch (RuntimeException e) {
            log.error("cancelPayment failed for id={}", id, e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping(value = "/return")
    public ResponseEntity<PaymentReturnResponse> handlePaymentReturn(HttpServletRequest request) {
        log.info("Received payment return from Inicis");

        try {
            // POST 파라미터에서 이니시스 결과 데이터 추출
            Map<String, String> params = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values.length > 0) {
                    params.put(key, values[0]);
                    log.info("Received parameter: {} = {}", key, values[0]);
                }
            });

            // 필수 파라미터 확인
            String resultCode = params.get("resultCode");
            if (resultCode == null) {
                log.error("Missing resultCode in payment return");
                return ResponseEntity.badRequest().body(
                        new PaymentReturnResponse(false, "결제 결과 정보가 없습니다.")
                );
            }

            log.info("Payment return resultCode: {}", resultCode);

            // 결제 성공인 경우 승인 처리
            if ("0000".equals(resultCode)) {
                PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest();
                confirmRequest.setAuthToken(params.get("authToken"));
                confirmRequest.setAuthUrl(params.get("authUrl"));
                confirmRequest.setNetCancelUrl(params.get("netCancelUrl"));
                confirmRequest.setMid(params.get("mid"));
                confirmRequest.setOrderNumber(params.get("oid"));
                confirmRequest.setPrice(params.get("price"));

                log.info("Processing payment confirmation for OID: {}", confirmRequest.getOrderNumber());

                // 결제 승인 처리
                var confirmedPayment = paymentService.confirmPayment(confirmRequest);

                return ResponseEntity.ok(
                        new PaymentReturnResponse(true, "결제가 성공적으로 완료되었습니다.", confirmedPayment)
                );
            } else {
                // 결제 실패
                String resultMsg = params.get("resultMsg");
                log.error("Payment failed with resultCode: {}, message: {}", resultCode, resultMsg);

                return ResponseEntity.ok(
                        new PaymentReturnResponse(false, resultMsg != null ? resultMsg : "결제가 실패했습니다.", resultCode)
                );
            }

        } catch (Exception e) {
            log.error("Error processing payment return", e);
            return ResponseEntity.internalServerError().body(
                    new PaymentReturnResponse(false, "결제 처리 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }
}

// PaymentReturnResponse DTO 클래스
class PaymentReturnResponse {
    private boolean success;
    private String message;
    private String resultCode;
    private Payment payment;

    public PaymentReturnResponse() {
    }

    public PaymentReturnResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public PaymentReturnResponse(boolean success, String message, Payment payment) {
        this.success = success;
        this.message = message;
        this.payment = payment;
    }

    public PaymentReturnResponse(boolean success, String message, String resultCode) {
        this.success = success;
        this.message = message;
        this.resultCode = resultCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}

// PaymentReturnRequest DTO 클래스 (이니시스에서 전달하는 파라미터들)
class PaymentReturnRequest {
    private String resultCode;
    private String resultMsg;
    private String authToken;
    private String authUrl;
    private String netCancelUrl;
    private String mid;
    private String oid;
    private String price;

    // 기본 생성자
    public PaymentReturnRequest() {}

    // Getter, Setter
    public String getResultCode() { return resultCode; }
    public void setResultCode(String resultCode) { this.resultCode = resultCode; }

    public String getResultMsg() { return resultMsg; }
    public void setResultMsg(String resultMsg) { this.resultMsg = resultMsg; }

    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }

    public String getAuthUrl() { return authUrl; }
    public void setAuthUrl(String authUrl) { this.authUrl = authUrl; }

    public String getNetCancelUrl() { return netCancelUrl; }
    public void setNetCancelUrl(String netCancelUrl) { this.netCancelUrl = netCancelUrl; }

    public String getMid() { return mid; }
    public void setMid(String mid) { this.mid = mid; }

    public String getOid() { return oid; }
    public void setOid(String oid) { this.oid = oid; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
}
