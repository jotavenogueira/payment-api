package com.joaovitor.payment_api;

import com.joaovitor.paymentapi.PaymentApiApplication;
import com.joaovitor.paymentapi.dto.CreatePaymentRequest;
import com.joaovitor.paymentapi.entity.Customer;
import com.joaovitor.paymentapi.entity.Payment;
import com.joaovitor.paymentapi.enums.PaymentMethod;
import com.joaovitor.paymentapi.enums.PaymentStatus;
import com.joaovitor.paymentapi.enums.TransactionStatus;
import com.joaovitor.paymentapi.exception.InvalidPaymentStatusException;
import com.joaovitor.paymentapi.repository.CustomerRepository;
import com.joaovitor.paymentapi.repository.PaymentRepository;
import com.joaovitor.paymentapi.repository.PaymentTransactionRepository;
import com.joaovitor.paymentapi.service.CustomerService;
import com.joaovitor.paymentapi.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = PaymentApiApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentApiApplicationTests {

    @Autowired MockMvc mvc;
    @Autowired PaymentService payments;
    @Autowired CustomerService customers;
    @Autowired PaymentRepository paymentRepository;
    @Autowired PaymentTransactionRepository transactionRepository;
    @Autowired CustomerRepository customerRepository;
    @Autowired PlatformTransactionManager transactionManager;

    private Payment payment;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        paymentRepository.deleteAll();
        customerRepository.deleteAll();
        Customer customer = new Customer();
        customer.setName("Cliente de teste");
        customer.setEmail("teste@example.com");
        customer.setDocument("12345678901");
        customer = customers.create(customer);
        payment = payments.create(new CreatePaymentRequest(
                customer.getId(), new BigDecimal("150.00"), "BRL", PaymentMethod.PIX));
    }

    @Test
    void processesPaymentAndReturnsHistory() throws Exception {
        var originalCreatedAt = payments.findById(payment.getId()).getCreatedAt();
        mvc.perform(post("/payments/{id}/process", payment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        var transactions = transactionRepository.findByPaymentIdOrderByCreatedAtAscIdAsc(payment.getId());
        assertThat(transactions).hasSize(1);
        var transaction = transactions.getFirst();
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(transaction.getAmount()).isEqualByComparingTo("150.00");
        assertThat(transaction.getTransactionId()).isNotBlank();
        assertThat(transaction.getCreatedAt()).isNotNull();
        assertThat(payments.findById(payment.getId()).getCreatedAt()).isEqualTo(originalCreatedAt);

        mvc.perform(get("/payments/{id}/transactions", payment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].paymentId").value(payment.getId()))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$[0].payment").doesNotExist());
    }

    @Test
    void repeatedProcessingAndCancellationOfApprovedPaymentReturnConflict() throws Exception {
        payments.process(payment.getId());
        mvc.perform(post("/payments/{id}/process", payment.getId()))
                .andExpect(status().isConflict());
        mvc.perform(patch("/payments/{id}/cancel", payment.getId()))
                .andExpect(status().isConflict());
        assertThat(transactionRepository.count()).isEqualTo(1);
        assertThat(payments.findById(payment.getId()).getStatus()).isEqualTo(PaymentStatus.APPROVED);
    }

    @Test
    void cancelledPaymentCannotBeProcessed() throws Exception {
        mvc.perform(patch("/payments/{id}/cancel", payment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        mvc.perform(post("/payments/{id}/process", payment.getId()))
                .andExpect(status().isConflict());
        assertThat(transactionRepository.count()).isZero();
    }

    @Test
    void missingPaymentReturnsNotFound() throws Exception {
        mvc.perform(post("/payments/{id}/process", Long.MAX_VALUE)).andExpect(status().isNotFound());
        mvc.perform(get("/payments/{id}/transactions", Long.MAX_VALUE)).andExpect(status().isNotFound());
        mvc.perform(patch("/payments/{id}/cancel", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void pendingPaymentHasEmptyHistory() throws Exception {
        mvc.perform(get("/payments/{id}/transactions", payment.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void invalidAmountIsRejectedBeforeSaving() throws Exception {
        mvc.perform(post("/payments").contentType("application/json")
                        .content("{\"customerId\":" + payment.getCustomer().getId()
                                + ",\"amount\":0,\"currency\":\"BRL\",\"paymentMethod\":\"PIX\"}"))
                .andExpect(status().isBadRequest());
        assertThat(paymentRepository.count()).isEqualTo(1);
    }

    @Test
    void rollbackUndoesBothApprovalAndTransaction() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            payments.process(payment.getId());
            status.setRollbackOnly();
        });
        assertThat(payments.findById(payment.getId()).getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(transactionRepository.count()).isZero();
    }

    @Test
    void concurrentProcessingCreatesOnlyOneTransaction() throws Exception {
        var results = race(() -> payments.process(payment.getId()),
                () -> payments.process(payment.getId()));
        assertThat(results).containsExactlyInAnyOrder("OK", "CONFLICT");
        assertThat(transactionRepository.count()).isEqualTo(1);
        assertThat(payments.findById(payment.getId()).getStatus()).isEqualTo(PaymentStatus.APPROVED);
    }

    @Test
    void cancellationAndProcessingCannotBothSucceed() throws Exception {
        var results = race(() -> payments.process(payment.getId()),
                () -> payments.cancel(payment.getId()));
        assertThat(results).containsExactlyInAnyOrder("OK", "CONFLICT");
        var finalStatus = payments.findById(payment.getId()).getStatus();
        assertThat(finalStatus).isIn(PaymentStatus.APPROVED, PaymentStatus.CANCELLED);
        assertThat(transactionRepository.count()).isEqualTo(finalStatus == PaymentStatus.APPROVED ? 1 : 0);
    }

    private List<String> race(Callable<Payment> first, Callable<Payment> second) throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var one = executor.submit(() -> attempt(first, ready, start));
            var two = executor.submit(() -> attempt(second, ready, start));
            try {
                assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            } finally {
                start.countDown();
            }
            return List.of(one.get(15, TimeUnit.SECONDS), two.get(15, TimeUnit.SECONDS));
        }
    }

    private String attempt(Callable<Payment> action, CountDownLatch ready, CountDownLatch start)
            throws Exception {
        ready.countDown();
        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Concorrência não iniciou a tempo");
        }
        try {
            action.call();
            return "OK";
        } catch (InvalidPaymentStatusException exception) {
            return "CONFLICT";
        }
    }
}
