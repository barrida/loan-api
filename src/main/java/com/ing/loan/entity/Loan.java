package com.ing.loan.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * @author Suleyman Yildirim
 */

@Entity
@Table(name = "loan")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a loan entity, including details about the loan amount, installments, customer, and loan status.")
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = "The unique identifier for the loan", example = "12345")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference
    @Schema(description = "The customer associated with this loan")
    private Customer customer; // Many loans can belong to one customer.

    @Column(nullable = false)
    @Schema(description = "The amount of the loan", example = "10000.00")
    @Positive
    private BigDecimal loanAmount;

    @Column(nullable = false)
    @Schema(description = "The number of installments for the loan", example = "12")
    @Positive
    private Integer numberOfInstallment;

    @Column(nullable = false)
    @Schema(description = "The date when the loan was created", example = "2024-01-15")
    private LocalDate createDate;

    @Column(nullable = false)
    @Schema(description = "Indicates whether the loan has been fully paid", example = "false")
    private Boolean isPaid;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "A list of installments associated with this loan")
    private List<LoanInstallment> installments; // A loan can have multiple installments.


    @PrePersist
    @PreUpdate
    public void validateInstallment() {
        List<Integer> allowedInstallments = Arrays.asList(6, 9, 12, 24);
        if (!allowedInstallments.contains(this.numberOfInstallment)) {
            throw new IllegalArgumentException("Invalid number of installments. Valid values are 6, 9, 12, or 24.");
        }
    }

}
