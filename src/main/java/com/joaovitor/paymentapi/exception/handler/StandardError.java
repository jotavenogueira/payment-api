package com.joaovitor.paymentapi.exception.handler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StandardError {

    private Instant timeStamp;
    private  Integer status;
    private String error;
    private String message;
    private String path;

}
