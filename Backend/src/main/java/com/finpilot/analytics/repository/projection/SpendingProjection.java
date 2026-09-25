package com.finpilot.analytics.repository.projection;

import java.math.BigDecimal;

public interface SpendingProjection {
    String getCategory();
    BigDecimal getAmount();
}
