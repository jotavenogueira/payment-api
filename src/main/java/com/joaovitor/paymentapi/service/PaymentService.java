package com.joaovitor.paymentapi.service;

import com.joaovitor.paymentapi.dto.CreatePaymentRequest;
import com.joaovitor.paymentapi.entity.Customer;
import com.joaovitor.paymentapi.entity.Payment;
import com.joaovitor.paymentapi.entity.PaymentTransaction;
import com.joaovitor.paymentapi.enums.TransactionStatus;
import com.joaovitor.paymentapi.repository.PaymentTransactionRepository;
import com.joaovitor.paymentapi.enums.PaymentStatus;
import com.joaovitor.paymentapi.exception.InvalidPaymentStatusException;
import com.joaovitor.paymentapi.exception.PaymentNotFoundException;
import com.joaovitor.paymentapi.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CustomerService customerService;
    private final PaymentTransactionRepository transactionRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            CustomerService customerService,
            PaymentTransactionRepository transactionRepository) {
        this.paymentRepository = paymentRepository;
        this.customerService = customerService;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Payment create(CreatePaymentRequest request) {
        Customer customer = customerService.findById(request.customerId());
        LocalDateTime now = LocalDateTime.now();

        Payment payment = new Payment();
        payment.setCustomer(customer);
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);

        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment findById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    @Transactional
    public Payment cancel(Long id) {
        Payment payment = findByIdForUpdate(id);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStatusException(
                    "Somente pagamentos pendentes podem ser cancelados"
            );
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setUpdatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment process(Long id) {
        Payment payment = findByIdForUpdate(id);
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStatusException(
                    "Somente pagamentos pendentes podem ser processados"
            );
        }

        LocalDateTime now = LocalDateTime.now();
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPayment(payment);
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setAmount(payment.getAmount());
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);

        // Simulação local: não existe chamada a um provedor de pagamentos.
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setUpdatedAt(now);
        transactionRepository.save(transaction);

        return paymentRepository.save(payment);
    }

    private Payment findByIdForUpdate(Long id) {
        return paymentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }
}
