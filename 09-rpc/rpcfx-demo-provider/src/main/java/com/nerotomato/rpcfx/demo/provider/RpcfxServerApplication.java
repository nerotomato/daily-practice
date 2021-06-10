package com.nerotomato.rpcfx.demo.provider;

import com.alibaba.fastjson.JSON;
import com.nerotomato.rpcfx.demo.api.OrderService;
import com.nerotomato.rpcfx.demo.api.UserService;
import com.nerotomato.rpcfx.api.RpcfxRequest;
import com.nerotomato.rpcfx.api.RpcfxResolver;
import com.nerotomato.rpcfx.api.RpcfxResponse;
import com.nerotomato.rpcfx.api.ServiceProviderDesc;
import com.nerotomato.rpcfx.server.RpcfxInvoker;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;

/**
 * @RestController注解相当于@ResponseBody ＋ @Controller合在一起的作用。
 * 在@controller注解中，返回的是字符串，或者是字符串匹配的模板名称，即直接渲染视图，与html页面配合使用的，
 * 在这种情况下，前后端的配合要求比较高，java后端的代码要结合html的情况进行渲染,使用model对象（或者modelandview）的数据将填充user视图中的相关属性，然后展示到浏览器，这个过程也可以称为渲染
 * 而在@restcontroller中，返回的应该是一个对象，即return一个user对象，这时，在没有页面的情况下，也能看到返回的是一个user对象对应的json字符串，而前端的作用是利用返回的json进行解析渲染页面，java后端的代码比较自由。
 * */
@SpringBootApplication
@RestController
public class RpcfxServerApplication {

    public static void main(String[] args) throws Exception {

        // start zk client
		/*RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		CuratorFramework client = CuratorFrameworkFactory.builder().connectString("localhost:2181").namespace("rpcfx").retryPolicy(retryPolicy).build();
		client.start();*/


        // register service
        // xxx "io.kimmking.rpcfx.demo.api.UserService"

		/*String userService = "io.kimking.rpcfx.demo.api.UserService";
		registerService(client, userService);
		String orderService = "io.kimking.rpcfx.demo.api.OrderService";
		registerService(client, orderService);*/


        // 进一步的优化，是在spring加载完成后，从里面拿到特定注解的bean，自动注册到zk

        SpringApplication.run(RpcfxServerApplication.class, args);
    }

    private static void registerService(CuratorFramework client, String service) throws Exception {
        ServiceProviderDesc userServiceSesc = ServiceProviderDesc.builder()
                .host(InetAddress.getLocalHost().getHostAddress())
                .port(8080).serviceClass(service).build();
        // String userServiceSescJson = JSON.toJSONString(userServiceSesc);

        try {
            if (null == client.checkExists().forPath("/" + service)) {
                client.create().withMode(CreateMode.PERSISTENT).forPath("/" + service, "service".getBytes());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        client.create().withMode(CreateMode.EPHEMERAL).
                forPath("/" + service + "/" + userServiceSesc.getHost() + "_" + userServiceSesc.getPort(), "provider".getBytes());
    }

    @Autowired
    RpcfxInvoker invoker;

    /*@PostMapping("/")
    public RpcfxResponse invoke(@RequestBody RpcfxRequest request) {
        return invoker.invoke(request);
    }*/
    //@PostMapping("/")
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public RpcfxResponse invoke(@RequestBody RpcfxRequest request) {
        //RpcfxRequest request
        String s = request.toString();

        //RpcfxRequest request2 = JSON.parseObject(s, RpcfxRequest.class);
        return invoker.invoke(request);
    }

    @Bean
    public RpcfxInvoker createInvoker(@Autowired RpcfxResolver resolver) {
        return new RpcfxInvoker(resolver);
    }

    @Bean
    public RpcfxResolver createResolver() {
        return new DemoResolver();
    }

    // 能否去掉name
    //

    // annotation


    @Bean(name = "io.kimmking.rpcfx.demo.api.UserService")
    public UserService createUserService() {
        return new UserServiceImpl();
    }

    @Bean(name = "io.kimmking.rpcfx.demo.api.OrderService")
    public OrderService createOrderService() {
        return new OrderServiceImpl();
    }

}
