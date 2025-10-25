package com.digcoo.fitech.common.param;

import com.digcoo.fitech.common.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResult {
    private boolean success;
    private Order order;
}
