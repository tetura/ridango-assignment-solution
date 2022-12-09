package com.rindago.payment.repositories;

import com.rindago.payment.entities.Payment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * A CrudRepository to handle database operations of payment entities
 */
@Repository
public interface PaymentRepository extends CrudRepository<Payment, Long> {

}
