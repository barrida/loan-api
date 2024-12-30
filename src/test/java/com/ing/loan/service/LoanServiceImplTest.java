package com.ing.loan.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ing.loan.entity.Customer;
import com.ing.loan.entity.Loan;
import com.ing.loan.entity.LoanInstallment;
import com.ing.loan.exception.ErrorCode;
import com.ing.loan.exception.InsufficientCreditLimitException;
import com.ing.loan.exception.LoanInstallmentNotFoundException;
import com.ing.loan.exception.LoanNotFoundException;
import com.ing.loan.repository.CustomerRepository;
import com.ing.loan.repository.LoanInstallmentRepository;
import com.ing.loan.repository.LoanRepository;
import com.ing.loan.request.LoanRequest;
import com.ing.loan.response.LoanResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * @author Suleyman Yildirim
 */

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;

    @InjectMocks
    private LoanServiceImpl loanService;

    private ObjectMapper objectMapper;

    private static final Long CUSTOMER_ID = 1L;
    private static final Long LOAN_ID = 1L;


    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testCreateLoan_Success() throws IOException {

        File jsonFile = new File("src/test/resources/expectedLoanInstallments.json");
        List<LoanInstallment> loanInstallments = objectMapper.readValue(jsonFile, new TypeReference<List<LoanInstallment>>(){});

        // Mock input
        LoanRequest loanRequest = LoanRequest.builder()
                .customerId(1L).loanAmount(BigDecimal.valueOf(10000))
                .installments(12)
                .interestRate(BigDecimal.valueOf(0.1)) // 10% interest rate
                .build();

        Customer mockCustomer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .creditLimit(BigDecimal.valueOf(500000))
                .usedCreditLimit(BigDecimal.valueOf(100000))
                .build();

        Loan mockLoan = Loan.builder()
                .id(1L)
                .loanAmount(BigDecimal.valueOf(10000))
                .numberOfInstallment(12)
                .createDate(LocalDate.now())
                .isPaid(false)
                .installments(loanInstallments)
                .build();

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(mockCustomer));
        when(loanRepository.save(any(Loan.class))).thenReturn(mockLoan);

        // Invoke the method
        LoanResponse response = loanService.createLoan(loanRequest);

        // Verify the results
        assertEquals(1L, response.getLoanId());
        assertEquals(12, response.getInstallments().size());
        assertEquals(BigDecimal.valueOf(916.67).setScale(2, RoundingMode.HALF_UP), response.getInstallments().get(0).getAmount());

        // Verify interactions
        verify(customerRepository, times(1)).findById(1L);
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void testListLoansByCustomer_Success() {
        // Arrange: Mock repository response
        Loan loan = new Loan();
        loan.setId(LOAN_ID);
        // Mock customer
        loan.setCustomer(Customer.builder()
                .id(1L)
                .build());
        List<Loan> loans = List.of(loan);
        Mockito.when(loanRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(loans));

        // Act: Call the service method
        List<Loan> result = loanService.listLoansByCustomer(CUSTOMER_ID);

        // Assert: Verify the result and interaction with the mock
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Mockito.verify(loanRepository).findByCustomerId(CUSTOMER_ID);
    }

    @Test
    void testListLoansByCustomer_NotFound() {
        // Arrange: Mock repository to return empty
        Mockito.when(loanRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.empty());

        // Act & Assert: Call the service method and expect an exception
        LoanNotFoundException exception = assertThrows(
                LoanNotFoundException.class,
                () -> loanService.listLoansByCustomer(CUSTOMER_ID)
        );
        Assertions.assertEquals("No loans found for customer with ID: " + CUSTOMER_ID, exception.getMessage());
    }

    @Test
    void testListInstallmentsByLoan_Success() {
        // Arrange: Mock repository response
        LoanInstallment installment = new LoanInstallment();
        installment.setLoan(Loan.builder().id(LOAN_ID).build());
        List<LoanInstallment> installments = List.of(installment);
        Mockito.when(loanInstallmentRepository.findByLoanId(LOAN_ID)).thenReturn(Optional.of(installments));

        // Act: Call the service method
        List<LoanInstallment> result = loanService.listInstallmentsByLoan(LOAN_ID);

        // Assert: Verify the result and interaction with the mock
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Mockito.verify(loanInstallmentRepository).findByLoanId(LOAN_ID);
    }

    @Test
    void testListInstallmentsByLoan_NotFound() {
        // Arrange: Mock repository to return empty
        Mockito.when(loanInstallmentRepository.findByLoanId(LOAN_ID)).thenReturn(Optional.empty());

        // Act & Assert: Call the service method and expect an exception
        LoanInstallmentNotFoundException exception = assertThrows(
                LoanInstallmentNotFoundException.class,
                () -> loanService.listInstallmentsByLoan(LOAN_ID)
        );
        Assertions.assertEquals("No installments found for loan with ID: " + LOAN_ID, exception.getMessage());
    }

    @Test
    void createLoan_WithInsufficientCreditLimit() {
        // Arrange
        LoanRequest loanRequest = LoanRequest.builder()
                .customerId(1L)
                .loanAmount(BigDecimal.valueOf(10000)) // Loan amount
                .installments(12) // Number of installments
                .interestRate(BigDecimal.valueOf(0.1)) // 10% interest rate
                .build();

        Customer mockCustomer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .creditLimit(BigDecimal.valueOf(15000)) // Credit limit
                .usedCreditLimit(BigDecimal.valueOf(15000)) // Used credit limit (no remaining credit)
                .build();

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(mockCustomer));

        // Act & Assert
        InsufficientCreditLimitException exception = assertThrows(
                InsufficientCreditLimitException.class,
                () -> loanService.createLoan(loanRequest)
        );
        assertEquals("Customer does not have enough credit limit for this loan", exception.getMessage());
        assertEquals(ErrorCode.INSUFFICIENT_CREDIT, exception.getErrorCode());


        // Verify interactions
        verify(customerRepository, times(1)).findById(1L);
        verify(loanRepository, times(0)).save(any(Loan.class)); // Loan should not be saved
    }


}