package com.nerotomato.mq.core.mymq;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;

@Data
@AllArgsConstructor
public class MyMqMessage<T> {
    private HashMap<String, Object> headers;
    private T body;
}
