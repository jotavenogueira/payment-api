package com.joaovitor.paymentapi.controller;

import com.joaovitor.paymentapi.entity.Customer;
import com.joaovitor.paymentapi.repository.CustomerRepository;
import com.joaovitor.paymentapi.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class    CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
            this.customerService = customerService;
    }

        @GetMapping
        public List<Customer> findAll() {
            return customerService.findAll();
        }

        @GetMapping("/{id}")
        public ResponseEntity<Customer> findById (@PathVariable Long id){
            Customer customer = customerService.findById(id);

            return ResponseEntity.ok().body(customer);
        }
        @PostMapping
        public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer){
            Customer newCustomer = customerService.create(customer);
            return  ResponseEntity.status(HttpStatus.CREATED).body(newCustomer);
        }

        @PutMapping("/{id}")
        public ResponseEntity<Customer> updateCustomer(@Valid @RequestBody Customer customer, @PathVariable Long id){
            Customer updatedCustomer = customerService.update(customer, id);

            return ResponseEntity.ok(updatedCustomer);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteCustomer(@PathVariable Long id){
            customerService.delete(id);

            return ResponseEntity.noContent().build();
        }
}
