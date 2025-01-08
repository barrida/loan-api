package com.ing.loan.service;

import com.ing.loan.entity.Customer;
import com.ing.loan.entity.Loan;
import com.ing.loan.entity.LoanInstallment;
import com.ing.loan.exception.ErrorCode;
import com.ing.loan.exception.LoanInstallmentNotFoundException;
import com.ing.loan.exception.LoanNotFoundException;
import com.ing.loan.repository.CustomerRepository;
import com.ing.loan.repository.LoanInstallmentRepository;
import com.ing.loan.repository.LoanRepository;
import com.ing.loan.response.LoanPaymentResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static com.ing.loan.exception.ErrorCode.LOAN_INSTALLMENT_NOT_FOUND;

/**
 * @author Suleyman Yildirim
 */

@Service
public class PaymentServiceImpl implements PaymentService{

    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;


    @Autowired
    public PaymentServiceImpl(LoanRepository loanRepository, CustomerRepository customerRepository, LoanInstallmentRepository loanInstallmentRepository) {
        this.loanRepository = loanRepository;
        this.customerRepository = customerRepository;
        this.loanInstallmentRepository = loanInstallmentRepository;
    }

    @Override
    @Transactional
    public LoanPaymentResponse payLoan(Long loanId, BigDecimal paymentAmount) {
        // Retrieve the loan
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(ErrorCode.LOAN_NOT_FOUND, "Loan %s not found".formatted(loanId)));

        List<LoanInstallment> filteredInstallments = filterInstallments(loanId);


        if (!filteredInstallments.isEmpty()) {

            // Calculate number of installments that can be paid
            BigDecimal installmentAmount = filteredInstallments.get(0).getAmount();
            int payableCount = paymentAmount.divideToIntegralValue(installmentAmount).intValue();
            BigDecimal totalPaid = BigDecimal.ZERO;
            int paidInstallmentsCount = 0;

            // List to track modified installments
            List<LoanInstallment> updatedInstallments = new ArrayList<>();

            for (LoanInstallment installment : filteredInstallments) {
                if (paidInstallmentsCount >= payableCount) break;

                // Fully pay the installment
                installment.setIsPaid(true);
                installment.setPaidAmount(installmentAmount);
                installment.setPaymentDate(LocalDate.now());
                totalPaid = totalPaid.add(installmentAmount);
                paidInstallmentsCount++;
                updatedInstallments.add(installment);
            }

            // Update loan status if all installments are paid
            boolean isLoanPaid = loan.getInstallments().stream().allMatch(LoanInstallment::getIsPaid);
            loan.setIsPaid(isLoanPaid);

            // Update customer's used credit limit if loan is fully paid
            if (isLoanPaid) {
                Customer customer = loan.getCustomer();
                customer.setUsedCreditLimit(customer.getUsedCreditLimit().subtract(loan.getLoanAmount()));
                customerRepository.save(customer);
            }

            // Save updates (only updated installments)
            loanInstallmentRepository.saveAll(updatedInstallments);
            loanRepository.save(loan);

            // Build and return response
            return LoanPaymentResponse.builder()
                    .isLoanPaid(isLoanPaid)
                    .paidInstallments(paidInstallmentsCount)
                    .totalAmountSpent(totalPaid)
                    .build();
        } else {
            throw new LoanInstallmentNotFoundException(LOAN_INSTALLMENT_NOT_FOUND, "No valid installments found for loan " + loanId);
        }
    }

    /**
     * Installments should be paid wholly or not at all.
     * 
     * Earliest installment shoul be paid first and if there are more money then you
     * should continue to next installment.
     * 
     * Installments have due date that still more than 3 calendar months cannot be
     * paid. So if we were in January, you could pay only for January, February and
     * March installments
     * 
     * @param loanId
     * @return
     */
    private List<LoanInstallment> filterInstallments(Long loanId) {
        // Determine the maximum due date (3 months from now)
        LocalDate maxDueDate = LocalDate.now()
                .plusMonths(3)
                .with(TemporalAdjusters.lastDayOfMonth());

        // Filter installments: unpaid and due within the next 3 months, sorted by due date
        return loanInstallmentRepository.findByLoanId(loanId).stream()
                .flatMap(Collection::stream)
                .filter(installment -> !installment.getIsPaid() && installment.getDueDate().isBefore(maxDueDate))
                .sorted(Comparator.comparing(LoanInstallment::getDueDate))
                .toList();
    }

}
