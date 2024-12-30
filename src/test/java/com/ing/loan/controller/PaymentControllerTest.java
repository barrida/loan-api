package com.ing.loan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ing.loan.exception.ErrorCode;
import com.ing.loan.exception.LoanNotFoundException;
import com.ing.loan.request.PaymentRequest;
import com.ing.loan.response.LoanPaymentResponse;
import com.ing.loan.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    private PaymentRequest validPaymentRequest;
    private LoanPaymentResponse validLoanPaymentResponse;

    @BeforeEach
    void setUp() {
        validPaymentRequest = PaymentRequest.builder()
                .loanId(1L)
                .paymentAmount(BigDecimal.valueOf(1000.0))
                .build();

        validLoanPaymentResponse = LoanPaymentResponse.builder()
                .paidInstallments(5)  // Example number of paid installments
                .totalAmountSpent(new BigDecimal("5000.00"))  // Example amount
                .isLoanPaid(true)  // Example loan status
                .build();

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testPayLoan_Success() throws Exception {
        // Arrange: Mock the service method
        when(paymentService.payLoan(anyLong(), any())).thenReturn(validLoanPaymentResponse);

        // Act & Assert: Perform a POST request and verify the result
        mockMvc.perform(post("/v1/pay-loan")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paidInstallments").value(5))
                .andExpect(jsonPath("$.totalAmountSpent").value(5000.00));
               // .andExpect(jsonPath("$.isLoanPaid").value(true));
    }

    @Test
    void testPayLoan_LoanNotFound() throws Exception {
        // Arrange: Mock the service method to throw exception
        when(paymentService.payLoan(1L, BigDecimal.valueOf(1000))).thenThrow(new LoanNotFoundException(ErrorCode.LOAN_NOT_FOUND, "Loan %s not found".formatted(1L)));

        // Act & Assert: Perform a POST request and expect a 404 response
        mockMvc.perform(post("/v1/pay-loan")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loanId\":1, \"paymentAmount\":1000}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPayLoan_InternalServerError() throws Exception {
        // Arrange: Mock the service method to throw an unexpected exception
        when(paymentService.payLoan(any(), any())).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert: Perform a POST request and expect a 500 response
        mockMvc.perform(post("/v1/pay-loan")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loanId\":1, \"paymentAmount\":1000}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testMakePayment_Invalid_PaymentRequestFields() throws Exception {
        // Arrange a payment request with invalid field values
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .loanId(1L)
                .paymentAmount(BigDecimal.valueOf(-100.00)) // Invalid negative payment amount
                .build();

        // Act & Assert
        mockMvc.perform(post("/v1/pay-loan")
                        .with(jwt().jwt((jwt) -> jwt.claim("scope", "ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(res -> assertInstanceOf(MethodArgumentNotValidException.class, res.getResolvedException()))
                .andExpect(jsonPath("$.error").value("Invalid user input"))
                .andExpect(jsonPath("$.message").value(containsInAnyOrder(
                        "Payment amount must be positive"
                )));
    }

    @Test
    void testMakePayment_Null_PaymentRequestFields() throws Exception {
        // Arrange a payment request with null fields
        PaymentRequest paymentRequest = PaymentRequest.builder().build();

        // Act & Assert
        mockMvc.perform(post("/v1/pay-loan")
                        .with(jwt().jwt((jwt) -> jwt.claim("scope", "ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(res -> assertInstanceOf(MethodArgumentNotValidException.class, res.getResolvedException()))
                .andExpect(jsonPath("$.error").value("Invalid user input"))
                .andExpect(jsonPath("$.message").value(containsInAnyOrder(
                        "Loan ID cannot be null",
                        "Payment amount is required"
                )));
    }


}
