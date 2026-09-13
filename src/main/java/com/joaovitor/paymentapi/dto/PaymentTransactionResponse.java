package com.joaovitor.paymentapi.dto;

import com.joaovitor.paymentapi.entity.PaymentTransaction;
import com.joaovitor.paymentapi.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentTransactionResponse(
        Long id,
        Long paymentId,
        String transactionId,
        TransactionStatus status,
        BigDecimal amount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PaymentTransactionResponse from(PaymentTransaction transaction) {
        return new PaymentTransactionResponse(
                transaction.getId(),
                transaction.getPayment().getId(),
                transaction.getTransactionId(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
