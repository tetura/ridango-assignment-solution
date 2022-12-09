package com.rindago.payment.controllers;

import com.rindago.payment.dtos.PaymentDto;
import com.rindago.payment.dtos.PaymentRequest;
import com.rindago.payment.services.PaymentService;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * THIS '/payment' ENDPOINT IS THE MAIN EXPECTED ENDPOINT IN THE ASSIGNMENT.
     *
     * An endpoint to make a payment
     * @param paymentRequest A DTO to transfer information of the payment to be made
     * @return The payment that has been made
     */
    @PostMapping("/payment")
    public ResponseEntity<PaymentDto> doPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        return ResponseEntity.ok().body(paymentService.makePayment(paymentRequest));
    }
}
