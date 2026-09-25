package com.finpilot.analytics.repository.projection;

import java.math.BigDecimal;

public interface CategorySpendingProjection {
    Long getCategoryId();
    String getCategory();
    String getCategoryGroup();
    BigDecimal getAmount();
    Long getTransactionCount();
}
