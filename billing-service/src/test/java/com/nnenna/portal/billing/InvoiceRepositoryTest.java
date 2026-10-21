package com.nnenna.portal.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class InvoiceRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    InvoiceRepository invoices;

    @Test
    void returnsOnlyTheCustomersOwnInvoices() {
        invoices.save(new Invoice(1L, new BigDecimal("10.00"), "a"));
        invoices.save(new Invoice(1L, new BigDecimal("20.00"), "b"));
        invoices.save(new Invoice(2L, new BigDecimal("30.00"), "c"));

        assertEquals(2, invoices.findByCustomerIdOrderByCreatedAtDesc(1L).size());
        assertEquals(1, invoices.findByCustomerIdOrderByCreatedAtDesc(2L).size());
    }
}
