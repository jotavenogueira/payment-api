package com.joaovitor.paymentapi.controller;

import com.joaovitor.paymentapi.dto.CreatePaymentRequest;
import com.joaovitor.paymentapi.dto.PaymentTransactionResponse;
import com.joaovitor.paymentapi.service.PaymentTransactionService;
import com.joaovitor.paymentapi.entity.Payment;
import com.joaovitor.paymentapi.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentTransactionService transactionService;

    public PaymentController(PaymentService paymentService,
                             PaymentTransactionService transactionService) {
        this.paymentService = paymentService;
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Payment> create(
            @Valid @RequestBody CreatePaymentRequest request) {
        Payment payment = paymentService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> findById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<Payment>> findAll() {
        return ResponseEntity.ok(paymentService.findAll());
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Payment> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.cancel(id));
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<Payment> process(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.process(id));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<PaymentTransactionResponse>> findTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.findByPaymentId(id));
    }
}
