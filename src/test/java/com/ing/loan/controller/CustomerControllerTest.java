package com.ing.loan.controller;

import com.ing.loan.entity.Customer;
import com.ing.loan.exception.CustomerExistsException;
import com.ing.loan.exception.ErrorCode;
import com.ing.loan.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Suleyman Yildirim
 */

@ExtendWith(MockitoExtension.class)
public class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private Customer customer;

    @BeforeEach
    public void setUp() {
        customer = Customer.builder()
                .id(1L)
                .name("Canan")
                .surname("Yildirim")
                .creditLimit(BigDecimal.valueOf(50000))
                .usedCreditLimit(BigDecimal.valueOf(20000))
                .build();
    }

    // Test the createCustomer endpoint
    @Test
    public void testCreateCustomer() {
        when(customerService.createCustomer(any(Customer.class))).thenReturn(customer);

        ResponseEntity<Customer> response = customerController.createCustomer(customer);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Canan", response.getBody().getName());
        assertEquals("Yildirim", response.getBody().getSurname());
        assertEquals(BigDecimal.valueOf(50000), response.getBody().getCreditLimit());

        verify(customerService, times(1)).createCustomer(any(Customer.class));
    }

    // Test the getCustomerById endpoint - Customer found
    @Test
    public void testGetCustomerById_CustomerFound() {
        when(customerService.findCustomerById(1L)).thenReturn(Optional.of(customer));

        ResponseEntity<Customer> response = customerController.getCustomerById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Canan", response.getBody().getName());
        assertEquals("Yildirim", response.getBody().getSurname());

        verify(customerService, times(1)).findCustomerById(1L);
    }

    // Test the getCustomerById endpoint - Customer not found
    @Test
    public void testGetCustomerById_CustomerNotFound() {

        // Arrange
        when(customerService.findCustomerById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Customer> response = customerController.getCustomerById(1L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(customerService, times(1)).findCustomerById(1L);
    }


    @Test
    void testRegisterCustomer_CustomerExistsException() throws Exception {

        // Arrange
        String expectedMessage = "Customer with ID %s already exists.".formatted(customer.getId());
        ErrorCode expectedErrorCode = ErrorCode.CUSTOMER_EXISTS;

        when(customerService.createCustomer(any(Customer.class))).thenThrow(new CustomerExistsException(expectedErrorCode, expectedMessage));

        // Act
        CustomerExistsException exception = assertThrows(CustomerExistsException.class, () -> {
            customerController.createCustomer(customer);
        });

        // Assert
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(expectedErrorCode,exception.getErrorCode());

    }

}
