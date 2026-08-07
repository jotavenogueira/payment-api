package com.joaovitor.paymentapi.repository;


import com.joaovitor.paymentapi.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
}
