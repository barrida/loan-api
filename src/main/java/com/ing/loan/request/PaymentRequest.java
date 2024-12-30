package com.ing.loan.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Represents a payment request for a loan. Contains details about the loan ID and the payment amount.")
public class PaymentRequest {
    @NotNull(message = "Loan ID cannot be null")
    @Schema(description = "The unique identifier for the loan", example = "98765")
    private Long loanId;

    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount must be positive")
    @Schema(description = "The amount to be paid for the loan", example = "500.00")
    private BigDecimal paymentAmount;
}
