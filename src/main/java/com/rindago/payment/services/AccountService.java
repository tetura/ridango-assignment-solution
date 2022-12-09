package com.rindago.payment.services;

import com.rindago.payment.dtos.AccountDto;
import com.rindago.payment.dtos.AccountRequest;
import com.rindago.payment.entities.Account;
import com.rindago.payment.exceptions.ExceptionCode;
import com.rindago.payment.exceptions.RequirementException;
import com.rindago.payment.repositories.AccountRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A service class to process account creation request and handle the entity-DTO transformation
 */
@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;

  /**
   * Creates a new account in the database
   * @param accountRequest A DTO holding and transferring information of the accounts to be created
   * @return Created accounts
   */
  @Transactional
  public List<AccountDto> createAccount(AccountRequest accountRequest) {
    var accountsToBeCreated = new ArrayList<Account>();
    accountRequest.getAccounts().forEach(account -> {
      if (BigDecimal.ZERO.compareTo(account.getBalance()) > 0) {
        throw new RequirementException(ExceptionCode.NEGATIVE_ACCOUNT_BALANCE);
      }
      var accountToBeCreated = new Account(); // Input DTOs -> entities to be saved in the DB
      accountToBeCreated.setName(account.getName());
      accountToBeCreated.setBalance(account.getBalance());
      accountsToBeCreated.add(accountToBeCreated);
    });
    accountRepository.saveAll(accountsToBeCreated); // Account entities are saved in the DB.

    var createdAccounts = new ArrayList<AccountDto>(); // Entities -> output DTOs
    accountsToBeCreated.forEach(account -> {
      var createdAccount = new AccountDto();
      createdAccount.setId(account.getId());
      createdAccount.setName(account.getName());
      createdAccount.setBalance(account.getBalance());
      createdAccounts.add(createdAccount);
    });

    return createdAccounts;
  }

}
