package com.joaovitor.paymentapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomerNotFoundException extends  RuntimeException{

    public CustomerNotFoundException(Long id){
        super("Cliente não encontrado com ID: " + id);
    }
}
