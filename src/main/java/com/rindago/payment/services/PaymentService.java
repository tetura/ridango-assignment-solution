package com.rindago.payment.services;

import com.rindago.payment.dtos.PaymentDto;
import com.rindago.payment.dtos.PaymentRequest;
import com.rindago.payment.entities.Payment;
import com.rindago.payment.exceptions.ExceptionCode;
import com.rindago.payment.exceptions.RequirementException;
import com.rindago.payment.repositories.AccountRepository;
import com.rindago.payment.repositories.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * A service class to process a payment request and handle the entity-DTO transformation
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final AccountRepository accountRepository;

  /**
   * Processes payment request, makes payment, and updates account balances accordingly
   * @param paymentRequest A DTO holding and transferring information of the payment to be made
   * @return Payment made
   */
  @Transactional
  public PaymentDto makePayment(PaymentRequest paymentRequest) {
    var amount = paymentRequest.getAmount();

    // ---
    // Requirements are checked in the following block and exception is thrown if inputs are problematic.
    if (paymentRequest.getAmount().scale() > 2) {
      throw new RequirementException(ExceptionCode.MORE_THAN_TWO_DECIMAL_PLACES);
    }

    if (BigDecimal.ZERO.compareTo(amount) >= 0) {
      throw new RequirementException(ExceptionCode.NOT_POSITIVE_PAYMENT_AMOUNT);
    }

    var sender = accountRepository.findById(paymentRequest.getSenderAccountId())
        .orElseThrow(() -> new RequirementException(ExceptionCode.SENDER_ACCOUNT_NOT_FOUND));

    var receiver = accountRepository.findById(paymentRequest.getReceiverAccountId())
        .orElseThrow(() -> new RequirementException(ExceptionCode.RECEIVER_ACCOUNT_NOT_FOUND));

    if (sender.getId().equals(receiver.getId())) {
      throw new RequirementException(ExceptionCode.SENDER_RECEIVER_THE_SAME);
    }

    if (amount.compareTo(sender.getBalance()) > 0) {
      throw new RequirementException(ExceptionCode.SENDER_ACCOUNT_BALANCE_GOING_NEGATIVE);
    }
    // ---

    sender.setBalance(sender.getBalance().subtract(amount)); // Sender's balance - amount
    accountRepository.save(sender); // Sender's balance is updated.
    receiver.setBalance(receiver.getBalance().add(amount)); // Receiver's balance + amount
    accountRepository.save(receiver); // Receiver's balance is updated.

    var paymentToBeDone = new Payment(); // Payment entities are saved in the DB.
    paymentToBeDone.setSenderAccountId(sender.getId());
    paymentToBeDone.setReceiverAccountId(receiver.getId());
    paymentToBeDone.setAmount(paymentRequest.getAmount());
    paymentToBeDone.setTimestamp(Instant.now());
    paymentRepository.save(paymentToBeDone);

    var completedPayment = new PaymentDto(); // Entity -> output DTO
    completedPayment.setId(paymentToBeDone.getId());
    completedPayment.setSenderAccountId(paymentToBeDone.getSenderAccountId());
    completedPayment.setReceiverAccountId(paymentToBeDone.getReceiverAccountId());
    completedPayment.setAmount(amount);
    completedPayment.setTimestamp(Instant.now());

    return completedPayment;
  }
}
