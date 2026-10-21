package com.nnenna.portal.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.client.RestClient;

/**
 * Consumer side of the contract: states exactly what billing-service needs from
 * GET /internal/customers/{id}. Running it writes target/pacts/billing-service-customer-service.json.
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "customer-service")
class CustomerClientPactTest {

    @Pact(consumer = "billing-service")
    RequestResponsePact customerExists(PactDslWithProvider builder) {
        return builder
                .given("customer 42 exists")
                .uponReceiving("a lookup of customer 42")
                .path("/internal/customers/42")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(new PactDslJsonBody()
                        .integerType("id", 42)
                        .stringType("fullName", "Ada Lovelace")
                        .stringType("email", "ada@example.com"))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "customerExists")
    void billingCanReadTheCustomerView(MockServer mockServer) {
        CustomerView view = RestClient.create(mockServer.getUrl())
                .get()
                .uri("/internal/customers/{id}", 42)
                .retrieve()
                .body(CustomerView.class);

        assertEquals(42L, view.id());
        assertEquals("Ada Lovelace", view.fullName());
        assertEquals("ada@example.com", view.email());
    }
}
