package com.finpilot.ai.service;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class FinanceIntentRouter {

    public FinanceIntent route(String message) {
        if (message == null || message.trim().isEmpty()) {
            return FinanceIntent.GENERAL_FINANCE;
        }

        String lower = message.toLowerCase(Locale.ROOT);

        // Specific multi-word keywords first
        if (lower.contains("net worth") || lower.contains("networth") || lower.contains("total assets") || lower.contains("account balance")) {
            return FinanceIntent.NET_WORTH;
        }

        if (lower.contains("cash flow") || lower.contains("cashflow") || lower.contains("save") || lower.contains("saved") || lower.contains("saving") || lower.contains("income") || lower.contains("earn") || lower.contains("salary")) {
            return FinanceIntent.CASH_FLOW;
        }

        if (lower.contains("subscription") || lower.contains("recurring") || lower.contains("autopay") || lower.contains("membership")) {
            return FinanceIntent.RECURRING;
        }

        if (lower.contains("budget") || lower.contains("allocated") || lower.contains("underfunded") || lower.contains("overfunded") || lower.contains("ready to assign")) {
            return FinanceIntent.BUDGET;
        }

        if (lower.contains("spend") || lower.contains("spent") || lower.contains("spending") || lower.contains("expense") || lower.contains("cost")) {
            return FinanceIntent.SPENDING;
        }

        if (lower.contains("transaction") || lower.contains("purchase") || lower.contains("bought") || lower.contains("history")) {
            return FinanceIntent.TRANSACTIONS;
        }

        return FinanceIntent.GENERAL_FINANCE;
    }
}
