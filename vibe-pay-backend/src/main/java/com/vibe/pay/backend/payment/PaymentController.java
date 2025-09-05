package com.vibe.pay.backend.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
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
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long id, @RequestBody Payment paymentDetails) {
        try {
            Payment updatedPayment = paymentService.updatePayment(id, paymentDetails);
            return ResponseEntity.ok(updatedPayment);
        } catch (RuntimeException e) {
            log.error("updatePayment failed for id={}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
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

    @PostMapping("/confirm")
    public ResponseEntity<Payment> confirmPayment(@RequestBody PaymentConfirmRequest request) {
        log.info("Received payment confirm request: authToken={}, oid={}, price={}", 
                request.getAuthToken(), request.getOid(), request.getPrice());
        try {
            Payment confirmedPayment = paymentService.confirmPayment(request);
            log.info("Payment confirmation successful for oid={}", request.getOid());
            return ResponseEntity.ok(confirmedPayment);
        } catch (Exception e) {
            log.error("confirmPayment failed. request={}", request, e);
            log.error("Error details: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null); // Or a more specific error response
        }
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<Payment> processPayment(@PathVariable Long id) {
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
    public ResponseEntity<Payment> cancelPayment(@PathVariable Long id) {
        try {
            Payment cancelledPayment = paymentService.cancelPayment(id);
            return ResponseEntity.ok(cancelledPayment);
        } catch (RuntimeException e) {
            log.error("cancelPayment failed for id={}", id, e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping(value = "/return")
    public ResponseEntity<Map<String, Object>> handlePaymentReturn(HttpServletRequest request) {
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
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "결제 결과 정보가 없습니다.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            log.info("Payment return resultCode: {}", resultCode);
            
            // 결제 성공인 경우 승인 처리
            if ("0000".equals(resultCode)) {
                PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest();
                confirmRequest.setAuthToken(params.get("authToken"));
                confirmRequest.setAuthUrl(params.get("authUrl"));
                confirmRequest.setNetCancelUrl(params.get("netCancelUrl"));
                confirmRequest.setMid(params.get("mid"));
                confirmRequest.setOid(params.get("oid"));
                confirmRequest.setPrice(params.get("price"));
                
                log.info("Processing payment confirmation for OID: {}", confirmRequest.getOid());
                
                // 결제 승인 처리
                var confirmedPayment = paymentService.confirmPayment(confirmRequest);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("payment", confirmedPayment);
                response.put("message", "결제가 성공적으로 완료되었습니다.");
                
                return ResponseEntity.ok(response);
            } else {
                // 결제 실패
                String resultMsg = params.get("resultMsg");
                log.error("Payment failed with resultCode: {}, message: {}", resultCode, resultMsg);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("resultCode", resultCode);
                response.put("message", resultMsg != null ? resultMsg : "결제가 실패했습니다.");
                
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            log.error("Error processing payment return", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "결제 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
