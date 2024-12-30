package com.ing.loan.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author Suleyman Yildirim
 */

@Getter
@Setter
@Builder
@Schema(description = "Represents a loan request with details about the customer, loan amount, interest rate, and repayment installments.")
public class LoanRequest {

    @NotNull(message = "Customer ID cannot be null")
    @Schema(description = "The unique identifier for the customer", example = "12345")
    private Long customerId;

    @NotNull(message = "Loan amount is required")
    @Positive(message = "Loan amount must be positive")
    @Schema(description = "The amount of the loan", example = "10000.00")
    private BigDecimal loanAmount;

    @NotNull(message = "Interest rate cannot be null")
    @DecimalMin(value = "0.1", message = "Interest rate must be at least 0.1")
    @DecimalMax(value = "0.5", message = "Interest rate cannot exceed 0.5")
    @Schema(description = "The interest rate applied to the loan", example = "0.05")
    private BigDecimal interestRate;

    @NotNull(message = "Number of installments is required")
    @Positive(message = "Installments must be positive")
    @Schema(description = "The number of installments for repayment", example = "12")
    private Integer installments;
}
