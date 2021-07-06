package com.nerotomato.mq.api;

import com.nerotomato.mq.core.custom.CustomMessage;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class HttpProducer implements Producer {

    /**
     * Spring自带http请求工具 RestTemplate 阻塞式
     */
    private final RestTemplate restTemplate = new RestTemplate();

    private Map<String, Object> properties;

    public HttpProducer(Map<String, Object> properties) {
        this.properties = properties;
    }


    @Override
    public Object send(String topic, String content) {
        String url = properties.get("url").toString();
        String brokerUrl = url + "/custom/send?topic=" + topic + "&content=" + content;
        HttpEntity<CustomMessage<String>> httpEntity = new HttpEntity<>(new CustomMessage<>(null, topic, content));
        ResponseEntity<Object> responseEntity = restTemplate.postForEntity(brokerUrl, httpEntity, Object.class);
        return responseEntity.getBody();
    }
}
