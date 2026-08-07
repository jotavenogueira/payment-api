package com.joaovitor.paymentapi.service;

import com.joaovitor.paymentapi.entity.Customer;
import com.joaovitor.paymentapi.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow();
    }

    public Customer create(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer update(Customer customer, Long id) {
        return customerRepository.save(customer);
    }

    public void delete(Long id) {
        Customer customer = this.findById(id);
        customerRepository.delete(customer);
    }
}