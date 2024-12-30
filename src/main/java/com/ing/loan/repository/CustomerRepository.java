package com.ing.loan.repository;

import com.ing.loan.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Suleyman Yildirim
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Add custom queries if needed
}
