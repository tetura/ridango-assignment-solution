package com.rindago.payment.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Exception codes and their explanatory messages to be returned in an HTTP bad request response
 * when the custom exception is thrown if there is a problematic scenario violating a requirement
 */
@Getter
@AllArgsConstructor
public enum ExceptionCode {
  SENDER_ACCOUNT_NOT_FOUND("Sender account could not be found"),
  RECEIVER_ACCOUNT_NOT_FOUND("Receiver account could not be found"),
  SENDER_RECEIVER_THE_SAME("Sender account and receiver account are the same (have the same ID)"),
  NOT_POSITIVE_PAYMENT_AMOUNT("Payment amount cannot be negative or zero"),
  NEGATIVE_ACCOUNT_BALANCE("Account balance cannot be negative"),
  SENDER_ACCOUNT_BALANCE_GOING_NEGATIVE(
      "A payment cannot make the sender account's balance drop to below zero"),
  MORE_THAN_TWO_DECIMAL_PLACES("Payment amount input can have 2 decimal places");

  private final String explanation;
}
