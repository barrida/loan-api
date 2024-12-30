package com.ing.loan.repository;

import com.ing.loan.entity.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Suleyman Yildirim
 */
@Repository
public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long> {
    // Optional return for the list of installments by Loan ID
    Optional<List<LoanInstallment>> findByLoanId(Long loanId);

}
