package com.joaovitor.paymentapi.entity;

import com.joaovitor.paymentapi.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column(unique = true)
    private String transactionId;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private BigDecimal amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentTransaction() {}
}
