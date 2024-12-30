package com.ing.loan.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Suleyman Yildirim
 */
@DataJpaTest
class LoanTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testValidateInstallment_success() {
        // Arrange: Create and persist a customer
        Customer customer = Customer.builder()
                .id(1L)
                .name("John")
                .surname("Doe")
                .creditLimit(new BigDecimal("50000"))
                .usedCreditLimit(new BigDecimal("0"))
                .build();

        // Arrange: Create and persist a loan
        Loan loan = Loan.builder()
                .customer(customer) // Associate loan with the existing customer
                .loanAmount(new BigDecimal("10000"))
                .numberOfInstallment(12)
                .createDate(LocalDate.now())
                .isPaid(false)
                .build();

        // Act & Assert
        Assertions.assertDoesNotThrow(() -> entityManager.persist(loan));
    }

    @Test
    void testValidateInstallment_failure() {
        // Arrange
        Loan loan = Loan.builder()
                .customer(Customer.builder().id(1L).build())
                .loanAmount(new BigDecimal("10000"))
                .numberOfInstallment(15) // Invalid installment
                .isPaid(false)
                .build();

        // Act & Assert
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> entityManager.persistAndFlush(loan)
        );
        Assertions.assertEquals("Invalid number of installments. Valid values are 6, 9, 12, or 24.", exception.getMessage());
    }
}
