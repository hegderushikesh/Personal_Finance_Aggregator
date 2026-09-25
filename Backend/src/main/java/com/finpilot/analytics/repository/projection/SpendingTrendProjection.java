package com.finpilot.analytics.repository.projection;

import java.math.BigDecimal;

public interface SpendingTrendProjection {
    String getMonth();
    BigDecimal getAmount();
}
