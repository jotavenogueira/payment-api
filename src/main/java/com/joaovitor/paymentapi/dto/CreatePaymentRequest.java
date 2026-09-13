package com.joaovitor.paymentapi.dto;

import com.joaovitor.paymentapi.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotNull(message = "O cliente é obrigatório")
        @Positive(message = "O ID do cliente deve ser positivo")
        Long customerId,

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor mínimo é 0.01")
        @Digits(integer = 12, fraction = 2,
                message = "O valor deve ter até 12 dígitos inteiros e 2 decimais")
        BigDecimal amount,

        @NotBlank(message = "A moeda é obrigatória")
        @Pattern(regexp = "BRL", message = "A moeda deve ser BRL")
        String currency,

        @NotNull(message = "A forma de pagamento é obrigatória")
        PaymentMethod paymentMethod
) {
}