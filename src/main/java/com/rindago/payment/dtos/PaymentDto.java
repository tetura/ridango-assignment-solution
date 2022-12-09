package com.rindago.payment.dtos;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

/**
 * A DTO covering and transferring information of a payment to be made from/to entity:
 * payment ID, the ID of the sender account, the ID of the receiver account,
 * the amount to be transferred, and the timestamp of the transaction
 */
@Data
public class PaymentDto {
  private Long id;
  private Long senderAccountId;
  private Long receiverAccountId;
  private BigDecimal amount;
  private Instant timestamp;
}
