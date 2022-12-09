package com.ridango.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.rindago.payment.PaymentApplication;
import com.rindago.payment.dtos.AccountDto;
import com.rindago.payment.dtos.AccountRequest;
import com.rindago.payment.dtos.PaymentRequest;
import com.rindago.payment.entities.Account;
import com.rindago.payment.exceptions.ExceptionCode;
import com.rindago.payment.repositories.AccountRepository;
import com.rindago.payment.repositories.PaymentRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = PaymentApplication.class)
@AutoConfigureMockMvc
class PaymentEndToEndTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private AccountRepository accountRepository;
  @Autowired
  private PaymentRepository paymentRepository;
  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * To clean up the database after the execution of each test case
   */
  @AfterEach
  void cleanDatabase() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "account", "payment");
  }

  @Test
  void test_1_createAccounts_makePaymentSuccessfully() throws Exception {
    // Create accounts edgeways with a POST request whose body is a list of DTO objects
    var accountRequest = new AccountRequest();
    var senderAccount = new AccountDto();
    var receiverAccount = new AccountDto();
    senderAccount.setName("Sender Account 1");
    senderAccount.setBalance(new BigDecimal("500.00"));
    receiverAccount.setName("Receiver Account 1");
    receiverAccount.setBalance(new BigDecimal("800.00"));
    accountRequest.setAccounts(new LinkedList<>(Arrays.asList(senderAccount, receiverAccount)));

    // Send a POST request to create accounts and verify the response
    var accountCreationResult = mockMvc.perform(post("/account/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(accountRequest)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn();

    var responseContent = accountCreationResult.getResponse().getContentAsString();

    senderAccount.setId(JsonPath.parse(responseContent)
        .read("$[0].id",
            Long.class)); // The newly generated account ID is retrieved from the response.
    receiverAccount.setId(JsonPath.parse(responseContent)
        .read("$[1].id",
            Long.class)); // The newly generated account ID is retrieved from the response.

    assertEquals(senderAccount.getName(), JsonPath.read(responseContent, "$[0].name"));
    assertEquals(receiverAccount.getName(), JsonPath.read(responseContent, "$[1].name"));
    assertEquals(senderAccount.getBalance().doubleValue(),
        JsonPath.read(responseContent, "$[0].balance"));
    assertEquals(receiverAccount.getBalance().doubleValue(),
        JsonPath.read(responseContent, "$[1].balance"));

    // Verify the created account records in the database over entity objects
    assertThat(accountRepository.findById(senderAccount.getId())).isNotEmpty();
    var createdSenderAccount = accountRepository.findById(senderAccount.getId()).orElseThrow();
    assertEquals(createdSenderAccount.getName(), senderAccount.getName());
    assertEquals(createdSenderAccount.getBalance(), senderAccount.getBalance());

    assertThat(accountRepository.findById(receiverAccount.getId())).isNotEmpty();
    var createdReceiverAccount = accountRepository.findById(receiverAccount.getId())
        .orElseThrow();
    assertEquals(createdReceiverAccount.getName(), receiverAccount.getName());
    assertEquals(createdReceiverAccount.getBalance(), receiverAccount.getBalance());

    // Make a payment
    var paymentRequest = new PaymentRequest();
    paymentRequest.setSenderAccountId(senderAccount.getId());
    paymentRequest.setReceiverAccountId(receiverAccount.getId());
    paymentRequest.setAmount(new BigDecimal("150.00"));

    // Send a POST request calling the '/payment' endpoint to make the payment and verify the response
    var paymentResult = mockMvc.perform(post("/payment/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(paymentRequest)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.senderAccountId").value(senderAccount.getId()))
        .andExpect(jsonPath("$.receiverAccountId").value(receiverAccount.getId()))
        .andExpect(jsonPath("$.amount").value(paymentRequest.getAmount().doubleValue()))
        .andReturn();

    var paymentId = JsonPath.parse(paymentResult.getResponse().getContentAsString())
        .read("$.id", Long.class); // The newly generated payment ID is retrieved from the response.

    // Verify the payment record in the database over entity object
    assertThat(paymentRepository.findById(paymentId)).isNotEmpty();
    var paymentMade = paymentRepository.findById(paymentId).orElseThrow();
    assertEquals(paymentRequest.getSenderAccountId(), paymentMade.getSenderAccountId());
    assertEquals(paymentRequest.getReceiverAccountId(), paymentMade.getReceiverAccountId());
    assertEquals(paymentRequest.getAmount(), paymentMade.getAmount());
    assertNotNull(paymentMade.getTimestamp());

    // Verify the balances of the sender and the receiver accounts after the payment
    var senderAccountAfterPayment = accountRepository.findById(senderAccount.getId())
        .orElseThrow();
    var receiverAccountAfterPayment = accountRepository.findById(receiverAccount.getId())
        .orElseThrow();
    assertEquals(new BigDecimal("350.00"),
        senderAccountAfterPayment.getBalance()); // 500 - 150 = 350
    assertEquals(new BigDecimal("950.00"),
        receiverAccountAfterPayment.getBalance()); // 800 + 150 = 950
  }

  @Test
  void test_2_accountCreationWithNegativeBalanceInput() throws Exception {
    // Try to create an account with negative balance
    var accountRequest = new AccountRequest();
    var accountToBeCreated = new AccountDto();
    accountToBeCreated.setName("Forbidden Account");
    accountToBeCreated.setBalance(new BigDecimal("-100.00"));
    accountRequest.setAccounts(Collections.singletonList(accountToBeCreated));

    // Verify that the POST request to create the account returned an error
    mockMvc.perform(post("/account/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(accountRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.errorCode").value(ExceptionCode.NEGATIVE_ACCOUNT_BALANCE.name()))
        .andExpect(jsonPath("$.errorMessage").value(
            ExceptionCode.NEGATIVE_ACCOUNT_BALANCE.getExplanation()));

    // Verify that no account has been created
    assertThat(accountRepository.findAll()).isEmpty();
  }

  @Test
  void test_3_paymentAmountWithMoreThanTwoDecimalPlaces() throws Exception {
    // Create accounts straightforwardly with entity objects through CrudRepository
    var senderAccount = this.createAndSaveAccountEntity("Sender Account 3",
        new BigDecimal("1000.00"));
    var receiverAccount = this.createAndSaveAccountEntity("Receiver Account 3",
        new BigDecimal("2500.00"));

    // Try to make a payment whose amount has more than two decimal places
    var paymentRequest = new PaymentRequest();
    paymentRequest.setSenderAccountId(senderAccount.getId());
    paymentRequest.setReceiverAccountId(receiverAccount.getId());
    paymentRequest.setAmount(new BigDecimal("150.503"));

    // Verify that the POST request to make the payment returned an error
    mockMvc.perform(post("/payment/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(paymentRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.errorCode").value(ExceptionCode.MORE_THAN_TWO_DECIMAL_PLACES.name()))
        .andExpect(jsonPath("$.errorMessage").value(
            ExceptionCode.MORE_THAN_TWO_DECIMAL_PLACES.getExplanation()));

    this.verifyThatNoPaymentMadeAndBalancesNotChanged(senderAccount, receiverAccount);
  }

  @Test
  void test_4_paymentAmountNegative() throws Exception {
    // Create accounts straightforwardly with entity objects through CrudRepository
    var senderAccount = this.createAndSaveAccountEntity("Sender Account 4",
        new BigDecimal("333.00"));
    var receiverAccount = this.createAndSaveAccountEntity("Receiver Account 4",
        new BigDecimal("666.55"));

    // Try to make a payment whose amount is negative
    var paymentRequest = new PaymentRequest();
    paymentRequest.setSenderAccountId(senderAccount.getId());
    paymentRequest.setReceiverAccountId(receiverAccount.getId());
    paymentRequest.setAmount(new BigDecimal("-75.00"));

    // Verify that the POST request to make the payment returned an error
    mockMvc.perform(post("/payment/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(paymentRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.errorCode").value(ExceptionCode.NOT_POSITIVE_PAYMENT_AMOUNT.name()))
        .andExpect(jsonPath("$.errorMessage").value(
            ExceptionCode.NOT_POSITIVE_PAYMENT_AMOUNT.getExplanation()));

    this.verifyThatNoPaymentMadeAndBalancesNotChanged(senderAccount, receiverAccount);
  }

  @Test
  void test_5_paymentAmountZero() throws Exception {
    // Create accounts straightforwardly with entity objects through CrudRepository
    var senderAccount = this.createAndSaveAccountEntity("Sender Account 5",
        new BigDecimal("600.00"));
    var receiverAccount = this.createAndSaveAccountEntity("Receiver Account 5",
        new BigDecimal("0.00"));

    // Try to make a payment whose amount is zero
    var paymentRequest = new PaymentRequest();
    paymentRequest.setSenderAccountId(senderAccount.getId());
    paymentRequest.setReceiverAccountId(receiverAccount.getId());
    paymentRequest.setAmount(BigDecimal.ZERO);

    // Verify that the POST request to make the payment returned an error
    mockMvc.perform(post("/payment/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(paymentRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.errorCode").value(ExceptionCode.NOT_POSITIVE_PAYMENT_AMOUNT.name()))
        .andExpect(jsonPath("$.errorMessage").value(
            ExceptionCode.NOT_POSITIVE_PAYMENT_AMOUNT.getExplanation()));

    this.verifyThatNoPaymentMadeAndBalancesNotChanged(senderAccount, receiverAccount);
  }

  @Test
  void test_6_paymentSenderAccountIdNotFound() throws Exception {
    // Create accounts straightforwardly with entity objects through CrudRepository
    var senderAccount = this.createAndSaveAccountEntity("Sender Account 6",
        new BigDecimal("750.85"));
    var receiverAccount = this.createAndSaveAccountEntity("Receiver Account 6",
        new BigDecimal("0.00"));

    // Try to make a payment from an unidentified sender
    var paymentRequest = new PaymentRequest();
    paymentRequest.setSenderAccountId(20L);
    paymentRequest.setReceiverAccountId(receiverAccount.getId());
    paymentRequest.setAmount(new BigDecimal("400.00"));

    // Verify that the POST request to make the payment returned an error
    mockMvc.perform(post("/payment/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(paymentRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.errorCode").value(ExceptionCode.SENDER_ACCOUNT_NOT_FOUND.name()))
        .andExpect(jsonPath("$.errorMessage").value(
            ExceptionCode.SENDER_ACCOUNT_NOT_FOUND.getExplanation()));

    this.verifyThatNoPaymentMadeAndBalancesNotChanged(senderAccount, receiverAccount);
  }

  @Test
  void test_7_paymentReceiverAccountIdNotFound() throws Exception {
    // Create accounts straightforwardly with entity objects through CrudRepository
    var senderAccount = this.createAndSaveAccountEntity("Sender Account 7",
        new BigDecimal("450.00"));
    var receiverAccount = this.createAndSaveAccountEntity("Receiver Account 7",
        new BigDecimal("700.00"));

    // Try to make a payment to an unidentified receiver
    var paymentRequest = new PaymentRequest();
    paymentRequest.setSenderAccountId(senderAccount.getId());
    paymentRequest.setReceiverAccountId(27L);
    paymentRequest.setAmount(new BigDecimal("22.00"));

    // Verify that the POST request to make the payment returned an error
    mockMvc.perform(post("/payment/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(paymentRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.errorCode").value(ExceptionCode.RECEIVER_ACCOUNT_NOT_FOUND.name()))
        .andExpect(jsonPath("$.errorMessage").value(
            ExceptionCode.RECEIVER_ACCOUNT_NOT_FOUND.getExplanation()));

    this.verifyThatNoPaymentMadeAndBalancesNotChanged(senderAccount, receiverAccount);
  }

  @Test
  void test_8_paymentWithTheSameSenderAndReceiver() throws Exception {
    // Create accounts straightforwardly with entity objects through CrudRepository
    var senderAccount = this.createAndSaveAccountEntity("Sender Account 8",
        new BigDecimal("200.00"));
    var receiverAccount = this.createAndSaveAccountEntity("Receiver Account 8",
        new BigDecimal("300.00"));

    // Try to make a payment from and to the same person
    var paymentRequest = new PaymentRequest();
    paymentRequest.setSenderAccountId(senderAccount.getId());
    paymentRequest.setReceiverAccountId(senderAccount.getId());
    paymentRequest.setAmount(new BigDecimal("200.00"));

    // Verify that the POST request to make the payment returned an error
    mockMvc.perform(post("/payment/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(paymentRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.errorCode").value(ExceptionCode.SENDER_RECEIVER_THE_SAME.name()))
        .andExpect(jsonPath("$.errorMessage").value(
            ExceptionCode.SENDER_RECEIVER_THE_SAME.getExplanation()));

    this.verifyThatNoPaymentMadeAndBalancesNotChanged(senderAccount, receiverAccount);
  }

  @Test
  void test_9_paymentAmountGreaterThanSenderBalance() throws Exception {
    // Create accounts straightforwardly with entity objects through CrudRepository
    var senderAccount = this.createAndSaveAccountEntity("Sender Account 9",
        new BigDecimal("500.00"));
    var receiverAccount = this.createAndSaveAccountEntity("Receiver Account 9",
        new BigDecimal("1000.00"));

    // Try to make a payment whose amount is greater than the sender's balance
    var paymentRequest = new PaymentRequest();
    paymentRequest.setSenderAccountId(senderAccount.getId());
    paymentRequest.setReceiverAccountId(receiverAccount.getId());
    paymentRequest.setAmount(new BigDecimal("600.00"));

    // Verify that the POST request to make the payment returned an error
    mockMvc.perform(post("/payment/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(paymentRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.errorCode").value(
            ExceptionCode.SENDER_ACCOUNT_BALANCE_GOING_NEGATIVE.name()))
        .andExpect(jsonPath("$.errorMessage").value(
            ExceptionCode.SENDER_ACCOUNT_BALANCE_GOING_NEGATIVE.getExplanation()));

    this.verifyThatNoPaymentMadeAndBalancesNotChanged(senderAccount, receiverAccount);
  }

  @Test
  void test_10_paymentAttemptWithAnEmptyMandatoryField() throws Exception {
    // Create accounts straightforwardly with entity objects through CrudRepository
    var senderAccount = this.createAndSaveAccountEntity("Sender Account 10",
        new BigDecimal("900.00"));
    var receiverAccount = this.createAndSaveAccountEntity("Receiver Account 10",
        new BigDecimal("200.00"));

    // Try to make a payment whose sender account ID, which is a mandatory field, is not set
    var paymentRequest = new PaymentRequest();
    paymentRequest.setReceiverAccountId(2L);
    paymentRequest.setAmount(new BigDecimal("350.00"));

    // Verify that the POST request returns a response with no content
    // because of the @NotNull annotation on the senderAccountId field of the PaymentRequest POJO
    mockMvc.perform(post("/payment/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(paymentRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(""));

    this.verifyThatNoPaymentMadeAndBalancesNotChanged(senderAccount, receiverAccount);
  }

  private Account createAndSaveAccountEntity(String accountName, BigDecimal accountBalance) {
    var account = new Account();
    account.setName(accountName);
    account.setBalance(accountBalance);
    return accountRepository.save(account);
  }

  private void verifyThatNoPaymentMadeAndBalancesNotChanged(Account senderAccount,
      Account receiverAccount) {
    // Verify that no payment has been made
    assertThat(paymentRepository.findAll()).isEmpty();

    // Verify that account balances have not been affected either
    assert senderAccount.getId() != null;
    var senderAccountAfterPayment = accountRepository.findById(senderAccount.getId())
        .orElseThrow();
    assert receiverAccount.getId() != null;
    var receiverAccountAfterPayment = accountRepository.findById(receiverAccount.getId())
        .orElseThrow();
    assertEquals(senderAccount.getBalance(), senderAccountAfterPayment.getBalance());
    assertEquals(receiverAccount.getBalance(), receiverAccountAfterPayment.getBalance());
  }
}
