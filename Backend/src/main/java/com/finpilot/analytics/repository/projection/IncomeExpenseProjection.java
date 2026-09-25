package com.finpilot.analytics.repository.projection;

import java.math.BigDecimal;

public interface IncomeExpenseProjection {
    String getMonth();
    BigDecimal getIncome();
    BigDecimal getExpense();
}
