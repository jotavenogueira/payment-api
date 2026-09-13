package com.joaovitor.paymentapi.service;

import com.joaovitor.paymentapi.dto.PaymentTransactionResponse;
import com.joaovitor.paymentapi.exception.PaymentNotFoundException;
import com.joaovitor.paymentapi.repository.PaymentRepository;
import com.joaovitor.paymentapi.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentTransactionService {

    private final PaymentTransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;

    public PaymentTransactionService(
            PaymentTransactionRepository transactionRepository,
            PaymentRepository paymentRepository) {
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public List<PaymentTransactionResponse> findByPaymentId(Long paymentId) {
        if (!paymentRepository.existsById(paymentId)) {
            throw new PaymentNotFoundException(paymentId);
        }
        return transactionRepository.findByPaymentIdOrderByCreatedAtAscIdAsc(paymentId)
                .stream()
                .map(PaymentTransactionResponse::from)
                .toList();
    }
}
