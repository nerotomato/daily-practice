package com.nerotomato.mq.demo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Order {
    private long orderId;
    private long timestamp;
    private String symbol;
    private Double price;
}
