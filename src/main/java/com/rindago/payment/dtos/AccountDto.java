package com.rindago.payment.dtos;

import java.math.BigDecimal;
import lombok.Data;

/**
 * A DTO covering and transferring account information from/to entity: account ID, account name, and
 * account balance
 */
@Data
public class AccountDto {

  private Long id;
  private String name;
  private BigDecimal balance;
}
