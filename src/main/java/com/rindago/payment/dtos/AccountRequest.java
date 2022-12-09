package com.rindago.payment.dtos;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * A DTO to transfer information of accounts (through account DTOs) to be created as requested
 */
@Data
public class AccountRequest {

  @NotNull
  private List<AccountDto> accounts;
}
