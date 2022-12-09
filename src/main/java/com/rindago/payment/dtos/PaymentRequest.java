package com.rindago.payment.dtos;

import java.math.BigDecimal;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * A DTO to transfer information of a payment to be made as requested: the ID of the sender account,
 * the ID of the receiver account, the amount to be transferred
 */
@Data
public class PaymentRequest {

  @NotNull
  private Long senderAccountId;

  @NotNull
  private Long receiverAccountId;

  @NotNull
  private BigDecimal amount;
}
