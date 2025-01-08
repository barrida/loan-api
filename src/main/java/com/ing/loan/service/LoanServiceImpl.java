package com.ing.loan.service;

import com.ing.loan.entity.Customer;
import com.ing.loan.entity.Loan;
import com.ing.loan.entity.LoanInstallment;
import com.ing.loan.exception.*;
import com.ing.loan.repository.CustomerRepository;
import com.ing.loan.repository.LoanInstallmentRepository;
import com.ing.loan.repository.LoanRepository;
import com.ing.loan.request.LoanRequest;
import com.ing.loan.response.LoanInstallmentResponse;
import com.ing.loan.response.LoanResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import static com.ing.loan.exception.ErrorCode.LOAN_INSTALLMENT_NOT_FOUND;
import static com.ing.loan.exception.ErrorCode.LOAN_NOT_FOUND;

/**
 * Implementation of LoanService.
 */
@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;


    public LoanServiceImpl(LoanRepository loanRepository, CustomerRepository customerRepository, LoanInstallmentRepository loanInstallmentRepository) {
        this.loanRepository = loanRepository;
        this.customerRepository = customerRepository;
        this.loanInstallmentRepository = loanInstallmentRepository;
    }

    @Override
    @Transactional
    public LoanResponse createLoan(LoanRequest loanRequest) {
        var customer = getCustomerById(loanRequest.getCustomerId());
        validateCreditLimit(customer, loanRequest);
        var loan = buildLoanEntity(customer, loanRequest);
        attachInstallments(loan, loanRequest);
        var savedLoan = loanRepository.save(loan);
        return buildLoanResponse(savedLoan);
    }

    @Override
    public List<Loan> listLoansByCustomer(Long customerId) {
        // Fetch loans for a given customerId
        return loanRepository.findByCustomerId(customerId)
                .filter(loans -> !loans.isEmpty())  // Ensure the list is not empty
                .orElseThrow(() -> new LoanNotFoundException(LOAN_NOT_FOUND, "No loans found for customer with ID: " + customerId));
    }

    @Override
    public List<LoanInstallment> listInstallmentsByLoan(Long loanId) {
        // Fetch installments for a given loanId
        return loanInstallmentRepository.findByLoanId(loanId)
                .filter(installments -> !installments.isEmpty())  // Ensure the list is not empty
                .orElseThrow(() -> new LoanInstallmentNotFoundException(LOAN_INSTALLMENT_NOT_FOUND, "No installments found for loan with ID: " + loanId));
    }

    private static List<LoanInstallment> getLoanInstallments(LoanRequest loanRequest, BigDecimal installmentAmount) {
        // Prepare the list of installments
        List<LoanInstallment> installments = new ArrayList<>();
        LocalDate nextDueDate = LocalDate.now().plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());

        for (int i = 0; i < loanRequest.getInstallments(); i++) {
            // Create each installment and add to the list
            var installment = LoanInstallment.builder()
                    .amount(installmentAmount)
                    .paidAmount(BigDecimal.ZERO)
                    .dueDate(nextDueDate)
                    .paymentDate(null)
                    .isPaid(false)
                    .build();

            installments.add(installment);

            // Update the due date for the next installment
            nextDueDate = nextDueDate.plusMonths(1);
        }
        return installments;
    }

    private LoanResponse buildLoanResponse(Loan loan) {
        List<LoanInstallmentResponse> installmentResponses = loan.getInstallments().stream()
                .map(installment -> LoanInstallmentResponse.builder()
                        .amount(installment.getAmount())
                        .dueDate(installment.getDueDate())
                        .isPaid(installment.getIsPaid())
                        .build()).toList();

        return LoanResponse.builder()
                .loanId(loan.getId())
                .installments(installmentResponses)
                .build();
    }

    private Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(ErrorCode.CUSTOMER_NOT_FOUND,
                        "Customer with ID %s not found.".formatted(customerId)));
    }

    private void validateCreditLimit(Customer customer, LoanRequest loanRequest) {
        BigDecimal loanAmount = calculateLoanAmount(loanRequest);
        if (customer.getCreditLimit().subtract(customer.getUsedCreditLimit()).compareTo(loanAmount) < 0) {
            throw new InsufficientCreditLimitException(ErrorCode.INSUFFICIENT_CREDIT,
                    "Customer does not have enough credit limit for this loan");
        }
    }

    private BigDecimal calculateLoanAmount(LoanRequest loanRequest) {
        return loanRequest.getLoanAmount().multiply(BigDecimal.ONE.add(loanRequest.getInterestRate()));
    }

        private Loan buildLoanEntity(Customer customer, LoanRequest loanRequest) {
        return Loan.builder()
                .customer(customer)
                .loanAmount(calculateLoanAmount(loanRequest))
                .numberOfInstallment(loanRequest.getInstallments())
                .createDate(LocalDate.now())
                .isPaid(false)
                .build();
    }

    private void attachInstallments(Loan loan, LoanRequest loanRequest) {
        BigDecimal installmentAmount = loan.getLoanAmount().divide(BigDecimal.valueOf(loanRequest.getInstallments()), RoundingMode.HALF_UP);
        List<LoanInstallment> installments = getLoanInstallments(loanRequest, installmentAmount);
        installments.forEach(installment -> installment.setLoan(loan));
        loan.setInstallments(installments);
    }
}
