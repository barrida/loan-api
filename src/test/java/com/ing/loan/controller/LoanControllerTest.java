package com.ing.loan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ing.loan.entity.Loan;
import com.ing.loan.entity.LoanInstallment;
import com.ing.loan.request.LoanRequest;
import com.ing.loan.response.LoanInstallmentResponse;
import com.ing.loan.response.LoanResponse;
import com.ing.loan.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = LoanController.class)
public class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoanService loanService;

    private LoanInstallment loanInstallment;

    @BeforeEach
    void setUp() {
        Loan loan = Loan.builder().id(1L).build();
        loanInstallment = LoanInstallment.builder()
                .loan(loan)
                .amount(BigDecimal.valueOf(100.0))
                .dueDate(LocalDate.now())
                .build();

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

    }

    @Test
    void testCreateLoan_successful_asAdmin() throws Exception {
        // Prepare mock request and response
        LoanRequest loanRequest = LoanRequest.builder().customerId(1L).loanAmount(BigDecimal.valueOf(10000.00)).interestRate(BigDecimal.valueOf(0.2)).installments(12).build();
        LoanResponse loanResponse = LoanResponse.builder().loanId(1L).installments(List.of(LoanInstallmentResponse.builder().amount(BigDecimal.valueOf(833.33)).dueDate(LocalDate.now().plusMonths(1)).isPaid(false).build())).build();

        // Mock service behavior
        Mockito.when(loanService.createLoan(any(LoanRequest.class))).thenReturn(loanResponse);

        // Perform POST request as ADMIN
        mockMvc.perform(post("/v1/create-loan")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(loanResponse)));

        // Verify service interaction
        Mockito.verify(loanService).createLoan(any(LoanRequest.class));
    }


    @Test
    void testCreateLoan_successful() throws Exception {
        // Prepare mock request
        LoanRequest loanRequest = LoanRequest.builder()
                .customerId(1L)
                .loanAmount(BigDecimal.valueOf(10000.00))
                .interestRate(BigDecimal.valueOf(0.2))
                .installments(12)
                .build();

        // Prepare mock response
        List<LoanInstallmentResponse> installments = List.of(
                LoanInstallmentResponse.builder()
                        .amount(BigDecimal.valueOf(833.33))
                        .dueDate(LocalDate.now().plusMonths(1))
                        .isPaid(false)
                        .build(),
                LoanInstallmentResponse.builder()
                        .amount(BigDecimal.valueOf(833.33))
                        .dueDate(LocalDate.now().plusMonths(2))
                        .isPaid(false)
                        .build()
        );

        LoanResponse loanResponse = LoanResponse.builder()
                .loanId(1L)
                .installments(installments)
                .build();

        // Mock service behavior
        Mockito.when(loanService.createLoan(any(LoanRequest.class))).thenReturn(loanResponse);

        // Perform POST request
        mockMvc.perform(post("/v1/create-loan")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(loanResponse)));

        // Verify service interaction
        Mockito.verify(loanService).createLoan(any(LoanRequest.class));
    }


    @Test
    void testCreateLoan_invalidRequest() throws Exception {
        // Prepare an invalid request (e.g., missing loanAmount)
        LoanRequest loanRequest = LoanRequest.builder()
                .customerId(1L)
                .installments(12)
                .build();

        // Perform POST request
        mockMvc.perform(post("/v1/create-loan")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isBadRequest());

        // Verify service is not called
        Mockito.verifyNoInteractions(loanService);
    }

    @Test
    void testListLoans_successful_asCustomer() throws Exception {
        // Mock loan data
        Loan loan = Loan.builder()
                .id(1L)
                .loanAmount(BigDecimal.valueOf(10000.00))
                .numberOfInstallment(12)
                .build();

        // Mock service behavior
        Mockito.when(loanService.listLoansByCustomer(1L)).thenReturn(List.of(loan));

        // Perform GET request
        mockMvc.perform(get("/v1/loans")
                        .param("customerId", "1")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].loanAmount").value(10000.00));
    }

    @Test
    void testListLoans_noContent_asCustomer() throws Exception {
        // Mock service behavior to return empty list
        Mockito.when(loanService.listLoansByCustomer(1L)).thenReturn(List.of());

        // Perform GET request
        mockMvc.perform(get("/v1/loans")
                        .param("customerId", "1")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isNoContent());
    }


    @Test
    void testListLoans_NotFound() throws Exception {
        // Arrange: Mock the service method to return an empty list
        when(loanService.listLoansByCustomer(1L)).thenReturn(List.of());

        // Act & Assert: Perform a GET request and expect a 204 No Content response
        mockMvc.perform(get("/v1/loans")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .param("customerId", "1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testListInstallments_Success() throws Exception {
        // Arrange: Mock the service method
        when(loanService.listInstallmentsByLoan(anyLong())).thenReturn(List.of(loanInstallment));

        // Act & Assert: Perform a GET request and verify the result
        mockMvc.perform(get("/v1/installments")
                        .param("loanId", "1")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andDo(print());
    }

    @Test
    void testListInstallments_NotFound() throws Exception {
        // Arrange: Mock the service method to return an empty list
        when(loanService.listInstallmentsByLoan(1L)).thenReturn(List.of());

        // Act & Assert: Perform a GET request and expect a 204 No Content response
        mockMvc.perform(get("/v1/installments")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .param("loanId", "1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testCreateLoan_Null_LoanRequestFields() throws Exception {
        // Arrange task with invalid task title
        LoanRequest task = LoanRequest.builder()
                .build();

        // Act & Assert
        mockMvc.perform(post("/v1/create-loan")
                        .with(jwt().jwt((jwt) -> jwt.claim("scope", "ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isBadRequest())
                .andExpect(res -> assertInstanceOf(MethodArgumentNotValidException.class, res.getResolvedException()))
                .andExpect(jsonPath("$.error").value("Invalid user input"))
                .andExpect(jsonPath("$.message").value(containsInAnyOrder(
                        "Interest rate cannot be null",
                        "Customer ID cannot be null",
                        "Loan amount is required",
                        "Number of installments is required"
                )));
    }

    @Test
    void testCreateLoan_Invalid_LoanRequestFields() throws Exception {
        // Arrange task with invalid task title
        LoanRequest task = LoanRequest.builder().customerId(1L)
                .loanAmount(BigDecimal.ZERO)
                .interestRate(BigDecimal.valueOf(0.6))
                .installments(-1)
                .build();

        // Act & Assert
        mockMvc.perform(post("/v1/create-loan")
                        .with(jwt().jwt((jwt) -> jwt.claim("scope", "ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isBadRequest())
                .andExpect(res -> assertInstanceOf(MethodArgumentNotValidException.class, res.getResolvedException()))
                .andExpect(jsonPath("$.error").value("Invalid user input"))
                .andExpect(jsonPath("$.message").value(containsInAnyOrder(
                        "Loan amount must be positive",
                        "Interest rate cannot exceed 0.5",
                        "Installments must be positive"
                )));
    }
}
