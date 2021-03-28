package com.nerotomato.nio;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * HttpClient测试demo，访问8801端口
 * Created by nero on 2021/3/28.
 */
public class HttpClientDemo {
    public static void main(String[] args) {
        //获取http客户端
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        //创建Get请求
        HttpGet httpGet = new HttpGet("http://localhost:8801");
        //响应模型
        CloseableHttpResponse response = null;
        // 由客户端执行(发送)Get请求
        try {
            response = httpClient.execute(httpGet);
            //从响应模型中获取响应实体
            HttpEntity httpEntity = response.getEntity();
            System.out.println("响应状态为：" + response.getStatusLine());
            if (httpEntity != null) {
                System.out.println("响应内容长度为：" + httpEntity.getContentLength());
                System.out.println("响应内容为：" + EntityUtils.toString(httpEntity));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
