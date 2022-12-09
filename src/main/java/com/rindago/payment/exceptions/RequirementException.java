package com.rindago.payment.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A custom exception to be thrown if there is a problematic scenario violating a requirement
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@Getter
public class RequirementException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    public RequirementException(ExceptionCode exceptionCode) {
        super(exceptionCode.getExplanation());
        this.exceptionCode = exceptionCode;
    }
}
