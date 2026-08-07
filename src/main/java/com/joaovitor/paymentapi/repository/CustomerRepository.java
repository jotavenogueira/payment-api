package com.joaovitor.paymentapi.repository;

import com.joaovitor.paymentapi.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
