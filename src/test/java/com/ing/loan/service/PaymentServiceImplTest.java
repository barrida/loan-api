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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Loan loan;
    private Customer customer;
    private LoanInstallment installment1;
    private LoanInstallment installment2;
    private LoanInstallment installment3;

    @BeforeEach
    void setUp() {
        // Mock customer
        customer = Customer.builder()
                .id(1L)
                .usedCreditLimit(BigDecimal.valueOf(10000))
                .build();

        // Mock loan installments
        installment1 = LoanInstallment.builder()
                .id(1L)
                .loan(loan)
                .amount(BigDecimal.valueOf(100))
                .paidAmount(BigDecimal.ZERO)
                .dueDate(LocalDate.now().minusMonths(1))
                .isPaid(false)
                .build();

        installment2 = LoanInstallment.builder()
                .id(2L)
                .loan(loan)
                .amount(BigDecimal.valueOf(100))
                .paidAmount(BigDecimal.ZERO)
                .dueDate(LocalDate.now())
                .isPaid(false)
                .build();

        installment3 = LoanInstallment.builder()
                .id(3L)
                .loan(loan)
                .amount(BigDecimal.valueOf(100))
                .paidAmount(BigDecimal.ZERO)
                .dueDate(LocalDate.now().plusMonths(1))
                .isPaid(false)
                .build();

        // Mock loan
        loan = Loan.builder()
                .id(1L)
                .loanAmount(BigDecimal.valueOf(10000))
                .isPaid(false)
                .customer(customer)
                .installments(Arrays.asList(installment1, installment2, installment3))
                .build();
    }

    @Test
    void payLoan_successfulPayment() {
        // Mock repository behavior
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanId(1L)).thenReturn(Optional.of(Arrays.asList(installment1, installment2, installment3)));

        // Call the method under test
        LoanPaymentResponse response = paymentService.payLoan(1L, BigDecimal.valueOf(200));

        // Verify installment updates
        assertEquals(2, response.getPaidInstallments());
        assertEquals(BigDecimal.valueOf(200), response.getTotalAmountSpent());
        assertEquals(false, response.isLoanPaid());

        // Verify repository interactions
        verify(loanInstallmentRepository).saveAll(Arrays.asList(installment1, installment2));
        verify(loanRepository).save(loan);
        verifyNoMoreInteractions(loanInstallmentRepository, loanRepository, customerRepository);
    }

    @Test
    void payLoan_loanNotFound() {
        // Mock repository behavior
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());

        // Verify exception thrown
        LoanNotFoundException exception = assertThrows(
                LoanNotFoundException.class,
                () -> paymentService.payLoan(1L, BigDecimal.valueOf(100))
        );

        assertEquals("Loan 1 not found", exception.getMessage());
        assertEquals(ErrorCode.LOAN_NOT_FOUND, exception.getErrorCode());

        // Verify no interactions with other repositories
        verifyNoInteractions(customerRepository, loanInstallmentRepository);
    }

    @Test
    void payLoan_failPaymentBeyondThreeMonths() {
        // Adjust installment3 due date beyond 3 months
        installment1.setDueDate(LocalDate.now().plusMonths(3).with(TemporalAdjusters.lastDayOfMonth()).plusDays(1));
        installment2.setDueDate(LocalDate.now().plusMonths(4));
        installment3.setDueDate(LocalDate.now().plusMonths(4));

        // Mock repository behavior
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanId(1L)).thenReturn(Optional.of(Arrays.asList(installment1, installment2, installment3)));

        // Call the method under test
        LoanInstallmentNotFoundException exception = assertThrows(
                LoanInstallmentNotFoundException.class,
                () -> paymentService.payLoan(1L, BigDecimal.valueOf(400))
        );

        // Assert exception message
        assertEquals("No valid installments found for loan 1", exception.getMessage());

        // Verify no updates to repositories
        verifyNoInteractions(customerRepository);
        verify(loanInstallmentRepository, times(0)).saveAll(anyList());
        verify(loanRepository, times(0)).save(any(Loan.class));
    }




}
