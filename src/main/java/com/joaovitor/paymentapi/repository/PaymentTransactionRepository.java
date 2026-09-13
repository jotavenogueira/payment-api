package com.joaovitor.paymentapi.repository;

import com.joaovitor.paymentapi.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByPaymentIdOrderByCreatedAtAscIdAsc(Long paymentId);
}
