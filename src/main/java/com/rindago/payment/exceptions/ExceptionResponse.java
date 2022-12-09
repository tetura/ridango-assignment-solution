package com.rindago.payment.exceptions;

import lombok.Data;

/**
 * The structure to wrap up an HTTP bad request response once the custom exception is thrown
 */
@Data
public class ExceptionResponse {
  private String errorCode;
  private String errorMessage;
}
