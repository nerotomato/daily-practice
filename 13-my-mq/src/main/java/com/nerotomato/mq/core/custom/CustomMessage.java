package com.nerotomato.mq.core.custom;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;

@Data
@AllArgsConstructor
public class CustomMessage<T> {
    private HashMap<String, Object> headers;
    private T body;
}
