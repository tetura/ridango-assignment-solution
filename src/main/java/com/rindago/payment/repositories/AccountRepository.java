package com.rindago.payment.repositories;

import com.rindago.payment.entities.Account;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * A CrudRepository to handle database operations of account entities
 */
@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {

}
