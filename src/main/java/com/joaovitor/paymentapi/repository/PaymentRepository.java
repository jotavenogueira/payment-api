package com.joaovitor.paymentapi.repository;

import com.joaovitor.paymentapi.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
