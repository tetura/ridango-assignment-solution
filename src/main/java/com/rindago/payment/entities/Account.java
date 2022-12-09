package com.rindago.payment.entities;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * An entity class covering information of an account to be created in the database
 * and transferring data from/to the relevant DTO
 */
@Data
@Entity
@Table(name = "account")
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Size(min = 1, max = 50)
  @Column(length = 50, unique = true, nullable = false)
  private String name;

  @NotNull
  @DecimalMin(value = "0")
  @Column(precision = 18, scale = 2, nullable = false)
  private BigDecimal balance;
}
