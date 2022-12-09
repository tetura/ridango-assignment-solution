package com.rindago.payment.controllers;

import com.rindago.payment.dtos.AccountDto;
import com.rindago.payment.dtos.AccountRequest;
import com.rindago.payment.services.AccountService;
import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/account", consumes = "application/json", produces = "application/json")
@AllArgsConstructor
public class AccountController {

  private final AccountService accountService;

  /**
   * An endpoint to create an account
   * @param accountRequest A DTO to transfer information of accounts to be created
   * @return List of created accounts
   */
  @PostMapping("/")
  public ResponseEntity<List<AccountDto>> createAccount(
      @Valid @RequestBody AccountRequest accountRequest) {
    return ResponseEntity.ok(accountService.createAccount(accountRequest));
  }

}
