package com.joaovitor.paymentapi.exception.handler;
import com.joaovitor.paymentapi.exception.CustomerNotFoundException;
import com.joaovitor.paymentapi.exception.InvalidPaymentStatusException;
import com.joaovitor.paymentapi.exception.PaymentNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<StandardError>  customerNotFound(CustomerNotFoundException customer, HttpServletRequest request){
        StandardError standardError = new StandardError();
        standardError.setTimeStamp(Instant.now());
        standardError.setMessage(customer.getMessage());
        standardError.setStatus(HttpStatus.NOT_FOUND.value());
        standardError.setError("customer");
        standardError.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(standardError);
    }

    @ExceptionHandler(InvalidPaymentStatusException.class)
    public ResponseEntity<StandardError>  InvalidPayment(InvalidPaymentStatusException invalid, HttpServletRequest request){
        StandardError standardError = new StandardError();
        standardError.setTimeStamp(Instant.now());
        standardError.setMessage(invalid.getMessage());
        standardError.setStatus(HttpStatus.BAD_REQUEST.value());
        standardError.setError("payment invalid");
        standardError.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(standardError);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<StandardError>  paymentNotFound(PaymentNotFoundException payment, HttpServletRequest request){
        StandardError standardError = new StandardError();
        standardError.setTimeStamp(Instant.now());
        standardError.setMessage(payment.getMessage());
        standardError.setStatus(HttpStatus.NOT_FOUND.value());
        standardError.setError("payment");
        standardError.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(standardError);
    }
}
