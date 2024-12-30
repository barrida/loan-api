-- Create Customer Table
CREATE TABLE customer (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    credit_limit DECIMAL(15, 2) NOT NULL,
    used_credit_limit DECIMAL(15, 2) NOT NULL
);

-- Create Loan Table
CREATE TABLE loan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    loan_amount DECIMAL(15, 2) NOT NULL,
    number_of_installment INT NOT NULL, -- This corresponds to the Installments enum values
    create_date DATE NOT NULL,
    is_paid BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_loan_customer FOREIGN KEY (customer_id) REFERENCES customer (id)
);

-- Create LoanInstallment Table
CREATE TABLE loan_installment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT,
    amount DECIMAL(15, 2) NOT NULL,
    paid_amount DECIMAL(15, 2),
    due_date DATE NOT NULL,
    payment_date DATE,
    is_paid BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_installment_loan FOREIGN KEY (loan_id) REFERENCES loan (id)
);
