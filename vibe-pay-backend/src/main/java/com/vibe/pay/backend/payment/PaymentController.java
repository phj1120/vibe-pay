package com.vibe.pay.backend.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        try {
            InicisPaymentParameters inicisParams = paymentService.initiatePayment(request);
            return ResponseEntity.ok(inicisParams);
        } catch (RuntimeException e) {
            log.error("initiatePayment failed. request={}", request, e);
            return ResponseEntity.badRequest().body(null); // Return null or specific error DTO
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<Payment> confirmPayment(@RequestBody PaymentConfirmRequest request) {
        try {
            Payment confirmedPayment = paymentService.confirmPayment(request);
            return ResponseEntity.ok(confirmedPayment);
        } catch (RuntimeException e) {
            log.error("confirmPayment failed. request={}", request, e);
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
}
