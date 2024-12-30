package com.ing.loan.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Suleyman Yildirim
 */
@Getter
@Setter
@Builder
@Schema(description = "Represents a response model for a loan installment, including details of the amount, due date, and payment status.")
public class LoanInstallmentResponse {

    @Schema(description = "The amount to be paid for this installment", example = "500.00")
    private BigDecimal amount;

    @Schema(description = "The due date for the installment payment", example = "2024-01-15")
    private LocalDate dueDate;

    @Schema(description = "Indicates whether the installment has been paid", example = "false")
    private boolean isPaid;
}

