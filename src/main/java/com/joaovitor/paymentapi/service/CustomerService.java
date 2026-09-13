package com.joaovitor.paymentapi.service;

import com.joaovitor.paymentapi.entity.Customer;
import com.joaovitor.paymentapi.exception.CustomerNotFoundException;
import com.joaovitor.paymentapi.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    public Customer create(Customer customer) {
        Customer newCustomer = new Customer();

        newCustomer.setName(customer.getName());
        newCustomer.setEmail(customer.getEmail());
        newCustomer.setDocument(customer.getDocument());
        newCustomer.setCreatedAt(LocalDateTime.now());

        return customerRepository.save(newCustomer);
    }

    public Customer update(Customer customer, Long id) {
        Customer existingCustomer = findById(id);

        existingCustomer.setName(customer.getName());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setDocument(customer.getDocument());

        return customerRepository.save(existingCustomer);
    }

    public void delete(Long id) {
        Customer customer = this.findById(id);
        customerRepository.delete(customer);
    }
}