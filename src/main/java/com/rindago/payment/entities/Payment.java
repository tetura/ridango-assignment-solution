package com.rindago.payment.entities;

import java.math.BigDecimal;
import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * An entity class covering information of a payment to be processed and to be created in the database
 * and transferring data from/to the relevant DTO
 */
@Data
@Entity
@Table(name = "payment")
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  private Long senderAccountId;

  @NotNull
  private Long receiverAccountId;

  @NotNull
  private BigDecimal amount;

  @NotNull
  private Instant timestamp;
}
