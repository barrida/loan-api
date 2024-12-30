package com.ing.loan.repository;

import com.ing.loan.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Loan entity.
 * @author Suleyman Yildirim
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    // Add custom queries if needed
    Optional<List<Loan>> findByCustomerId(Long customerId);
}
