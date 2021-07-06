package com.nerotomato.mq.api;

import com.nerotomato.mq.core.custom.CustomMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class HttpConsumer implements Consumer {

    private final RestTemplate restTemplate = new RestTemplate();

    private Map<String, Object> properties;

    public HttpConsumer(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public List<LinkedHashMap> poll(int num) {
        String topic = properties.get("topic").toString();
        String group = properties.get("group").toString();
        String url = properties.get("url").toString();
        String brokerUrl = url + "/custom/poll?topic=" + topic + "&group=" + group + "&num=" + num;
        long startTime = System.currentTimeMillis();
        ResponseEntity<List> responseEntity = restTemplate.getForEntity(brokerUrl, List.class);
        long endTime = System.currentTimeMillis();
        log.info("The time cost of Consumer is {} milliseconds", endTime - startTime);
        return responseEntity.getBody();
    }
}
