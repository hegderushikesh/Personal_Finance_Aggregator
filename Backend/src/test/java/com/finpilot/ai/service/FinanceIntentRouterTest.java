package com.finpilot.ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinanceIntentRouterTest {

    private FinanceIntentRouter router;

    @BeforeEach
    void setUp() {
        router = new FinanceIntentRouter();
    }

    @Test
    @DisplayName("Should route spending queries to SPENDING")
    void testSpendingIntent() {
        assertEquals(FinanceIntent.SPENDING, router.route("Where did I spend the most this month?"));
        assertEquals(FinanceIntent.SPENDING, router.route("What are my highest expenses?"));
        assertEquals(FinanceIntent.SPENDING, router.route("How much was spent on shopping?"));
    }

    @Test
    @DisplayName("Should route budget queries to BUDGET")
    void testBudgetIntent() {
        assertEquals(FinanceIntent.BUDGET, router.route("How is my budget looking?"));
        assertEquals(FinanceIntent.BUDGET, router.route("Are any categories underfunded?"));
        assertEquals(FinanceIntent.BUDGET, router.route("What is ready to assign?"));
    }

    @Test
    @DisplayName("Should route recurring queries to RECURRING")
    void testRecurringIntent() {
        assertEquals(FinanceIntent.RECURRING, router.route("What are my subscriptions?"));
        assertEquals(FinanceIntent.RECURRING, router.route("Show my recurring bills"));
        assertEquals(FinanceIntent.RECURRING, router.route("Do I have any autopay memberships?"));
    }

    @Test
    @DisplayName("Should route cash flow queries to CASH_FLOW")
    void testCashFlowIntent() {
        assertEquals(FinanceIntent.CASH_FLOW, router.route("How much did I save this month?"));
        assertEquals(FinanceIntent.CASH_FLOW, router.route("What is my cash flow?"));
        assertEquals(FinanceIntent.CASH_FLOW, router.route("What was my total income earned?"));
    }

    @Test
    @DisplayName("Should route net worth queries to NET_WORTH")
    void testNetWorthIntent() {
        assertEquals(FinanceIntent.NET_WORTH, router.route("What is my net worth?"));
        assertEquals(FinanceIntent.NET_WORTH, router.route("Show my total assets and liabilities"));
    }

    @Test
    @DisplayName("Should route transactions queries to TRANSACTIONS")
    void testTransactionsIntent() {
        assertEquals(FinanceIntent.TRANSACTIONS, router.route("Show my recent transactions"));
        assertEquals(FinanceIntent.TRANSACTIONS, router.route("What was my last purchase?"));
    }

    @Test
    @DisplayName("Should default unknown queries to GENERAL_FINANCE")
    void testDefaultIntent() {
        assertEquals(FinanceIntent.GENERAL_FINANCE, router.route("Hello there!"));
        assertEquals(FinanceIntent.GENERAL_FINANCE, router.route("Give me general financial advice"));
        assertEquals(FinanceIntent.GENERAL_FINANCE, router.route(""));
        assertEquals(FinanceIntent.GENERAL_FINANCE, router.route(null));
    }
}
