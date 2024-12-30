package com.ing.loan.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Suleyman Yildirim
 */

@Entity
@Table(name = "loan_installment")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents an installment of a loan, including details about the amount, due date, payment date, and payment status.")
public class LoanInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = "The unique identifier for the loan installment", example = "12345")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    @JsonBackReference
    @Schema(description = "The loan associated with this installment")
    private Loan loan; // Many installments belong to one loan.

    @Column(nullable = false)
    @Schema(description = "The amount to be paid for this installment", example = "500.00")
    private BigDecimal amount;

    @Column(nullable = false)
    @Schema(description = "The amount that has been paid for this installment", example = "500.00")
    private BigDecimal paidAmount;

    @Column(nullable = false)
    @Schema(description = "The due date for the installment payment", example = "2024-01-15")
    private LocalDate dueDate;

    @Column(nullable = false)
    @Schema(description = "The date when the installment was paid", example = "2024-01-14")
    private LocalDate paymentDate;

    @Column(nullable = false)
    @Schema(description = "Indicates whether the installment has been paid", example = "true")
    private Boolean isPaid;
}