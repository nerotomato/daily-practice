# **1.将服务端写死查找接口实现类变成泛型和反射**

**将resolve方法改成泛型，并自定义MyBeanFactory工厂，模拟spring IOC进行反射处理**

```java
package com.nerotomato.rpcfx.demo.provider;

import com.nerotomato.rpcfx.api.RpcfxResolver;
import com.nerotomato.rpcfx.demo.provider.factory.MyBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DemoResolver<T> implements RpcfxResolver, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public T resolve(String serviceClass) {
        T bean = null;
        try {
            bean = (T) MyBeanFactory.getBean(serviceClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bean;
    }

    /*@Override
    public Object resolve(String serviceClass) {
        return this.applicationContext.getBean(serviceClass);
    }*/

}
```

```
package com.nerotomato.rpcfx.demo.provider.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义bean工厂，简单实现spring ioc功能
 * 从springbeans.properties配置文件读取bean配置
 *
 * Created by nero on 2021/6/2.
 */
public class MyBeanFactory {

    private static Map<String, String> beanMap = new ConcurrentHashMap();

    public static Object getBean(String serviceClass) throws Exception {
        beanMap = getBeanMap();
        String beanName = null;
        Object bean = null;
        if (null != serviceClass && serviceClass.contains(".")) {
            beanName = serviceClass.substring(serviceClass.lastIndexOf(".") + 1);
        } else if (null != serviceClass) {
            beanName = serviceClass;
        } else {
            throw new Exception("Service:" + serviceClass + " can not be null!");
        }
        if (!beanMap.containsKey(beanName)) {
            throw new Exception("Service:" + serviceClass + " not found exception!");
        }
        try {
            Class<?> clazz = Class.forName(beanMap.get(beanName));
            bean = clazz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return bean;
    }

    private static Map<String, String> getBeanMap() {
        Map<String, String> map = new ConcurrentHashMap<>();
        Properties props = new Properties();
        InputStream inputStream = MyBeanFactory.class.getClassLoader().getResourceAsStream("springbeans.properties");
        try {
            props.load(inputStream);
            Enumeration<?> enums = props.propertyNames();
            while (enums.hasMoreElements()) {
                String beanName = (String) enums.nextElement();
                System.out.println(beanName);
                map.put(beanName, props.getProperty(beanName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
```

**自定义MyBeanFactory工厂**

```java
package com.nerotomato.rpcfx.demo.provider.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义bean工厂，简单实现spring ioc功能
 * 从springbeans.properties配置文件读取bean配置
 *
 * Created by nero on 2021/6/2.
 */
public class MyBeanFactory {

    private static Map<String, String> beanMap = new ConcurrentHashMap();

    public static Object getBean(String serviceClass) throws Exception {
        beanMap = getBeanMap();
        String beanName = null;
        Object bean = null;
        if (null != serviceClass && serviceClass.contains(".")) {
            beanName = serviceClass.substring(serviceClass.lastIndexOf(".") + 1);
        } else if (null != serviceClass) {
            beanName = serviceClass;
        } else {
            throw new Exception("Service:" + serviceClass + " can not be null!");
        }
        if (!beanMap.containsKey(beanName)) {
            throw new Exception("Service:" + serviceClass + " not found exception!");
        }
        try {
            Class<?> clazz = Class.forName(beanMap.get(beanName));
            bean = clazz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return bean;
    }

    private static Map<String, String> getBeanMap() {
        Map<String, String> map = new ConcurrentHashMap<>();
        Properties props = new Properties();
        InputStream inputStream = MyBeanFactory.class.getClassLoader().getResourceAsStream("springbeans.properties");
        try {
            props.load(inputStream);
            Enumeration<?> enums = props.propertyNames();
            while (enums.hasMoreElements()) {
                String beanName = (String) enums.nextElement();
                System.out.println(beanName);
                map.put(beanName, props.getProperty(beanName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
```

**springbeans.properties配置文件,用于自定义bean工厂读取配置**

```properties
UserService=com.nerotomato.rpcfx.demo.provider.UserServiceImpl
OrderService=com.nerotomato.rpcfx.demo.provider.OrderServiceImpl
```

# **2.将客户端动态代理改成 AOP，添加异常处理**

**使用ByteBuddy实现动态代理**



```java
package com.nerotomato.rpcfx.client;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.nerotomato.rpcfx.api.*;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public final class Rpcfx {

    static {
        //fastjson 添加autotype白名单
        ParserConfig.getGlobalInstance().addAccept("com.nerotomato");
    }

    public static <T, filters> T createFromRegistry(final Class<T> serviceClass, final String zkUrl, Router router, LoadBalancer loadBalance, Filter filter) {

        // 加filte之一

        // curator Provider list from zk
        List<String> invokers = new ArrayList<>();
        // 1. 简单：从zk拿到服务提供的列表
        // 2. 挑战：监听zk的临时节点，根据事件更新这个list（注意，需要做个全局map保持每个服务的提供者List）

        List<String> urls = router.route(invokers);

        String url = loadBalance.select(urls); // router, loadbalance

        return (T) create(serviceClass, url, filter);

    }

    public static <T> T create(final Class<T> serviceClass, final String url, Filter... filters) {

        // 0. 替换动态代理 -> AOP
        return (T) Proxy.newProxyInstance(Rpcfx.class.getClassLoader(),
                new Class[]{serviceClass}, new RpcfxInvocationHandler(serviceClass, url, filters));
    }

    //实现AOP 使用bytebuddy实现动态代理
    public static <T> T createByteBuddyDynamicProxy(final Class<T> serviceClass, final String url, Filter... filters) throws Exception {
        return (T) new ByteBuddy().subclass(Object.class)
                .implement(serviceClass)
                //.method(ElementMatchers.any())
                .intercept(InvocationHandlerAdapter.of(new RpcfxInvocationHandler(serviceClass, url, filters)))
                .make().load(Rpcfx.class.getClassLoader())
                .getLoaded().getDeclaredConstructor()
                .newInstance();
    }

    public static class RpcfxInvocationHandler implements InvocationHandler {

        public static final MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");

        private final Class<?> serviceClass;
        private final String url;
        private final Filter[] filters;

        public <T> RpcfxInvocationHandler(Class<T> serviceClass, String url, Filter... filters) {
            this.serviceClass = serviceClass;
            this.url = url;
            this.filters = filters;
        }

        // 可以尝试，自己去写对象序列化，二进制还是文本的，，，rpcfx是xml自定义序列化、反序列化，json: code.google.com/p/rpcfx
        // int byte char float double long bool
        // [], data class

        @Override
        public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {

            // 加filter地方之二
            // mock == true, new Student("hubao");

            RpcfxRequest request = new RpcfxRequest();
            request.setServiceClass(this.serviceClass.getName());
            request.setMethod(method.getName());
            request.setParams(params);

            if (null != filters) {
                for (Filter filter : filters) {
                    if (!filter.filter(request)) {
                        return null;
                    }
                }
            }

            RpcfxResponse response = post(request, url);

            // 加filter地方之三
            // Student.setTeacher("cuijing");

            // 这里判断response.status，处理异常
            // 考虑封装一个全局的RpcfxException
            if (!response.isStatus()) {
                //讲服务端异常信息返回给客户端
                return JSON.parse(response.getException().toString());
            }
            return JSON.parse(response.getResult().toString());
        }

        private RpcfxResponse post(RpcfxRequest req, String url) throws Exception {
            String reqJson = JSON.toJSONString(req);
            System.out.println("req json: " + reqJson);

            // 1.可以复用client
            // 2.尝试使用httpclient或者netty client
            /*OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(JSONTYPE, reqJson))
                    .build();
            String respJson = client.newCall(request).execute().body().string();*/
            RpcfxResponse response = NettyHttpClient.connect(reqJson, url);
            //System.out.println("resp json: " + respJson);
            //return JSON.parseObject(respJson, RpcfxResponse.class);
            return response;
        }
    }
}

```

# 3.使用 Netty+HTTP 作为 client 端传输方式



**客户端使用netty发送HTTP请求并获取响应**

**netty客户端代码：NettyHttpClient**

```java
package com.nerotomato.rpcfx.client;

import com.nerotomato.rpcfx.api.RpcfxResponse;
import com.nerotomato.rpcfx.client.handler.HttpInboundHandler;
import com.nerotomato.rpcfx.client.initializer.HttpInboundInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

/**
 * Created by nero on 2021/6/3.
 */
@Slf4j
public class NettyHttpClient {

    public static RpcfxResponse connect(String requestJson, String url) throws Exception {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        /**
         * 客户端业务处理handler
         */
        HttpInboundHandler httpInboundHandler = new HttpInboundHandler();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .handler(new HttpInboundInitializer(httpInboundHandler));
            //构建uri
            URI uri = new URI(url);

            //发起同步连接
            //ChannelFuture channelFuture = bootstrap.connect(uri.getHost(), uri.getPort());

            //发起异步连接
            ChannelFuture channelFuture = bootstrap.connect(uri.getHost(), uri.getPort()).sync();

            String[] arr = url.split(":" + uri.getPort());
            String requestPathStr = arr[1];
            //去除前面的ip和端口号，只保留后面的请求路径
            URI requestUri = new URI(requestPathStr);
            // 构建http请求
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.POST, requestUri.toASCIIString(),
                    Unpooled.wrappedBuffer(requestJson.getBytes()));
            request.headers().set(HttpHeaderNames.HOST, uri.getHost());
            request.headers().set(HttpHeaderNames.CONNECTION,
                    HttpHeaderNames.CONNECTION);
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH,
                    request.content().readableBytes());
            request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");

            // 发送http请求
            httpInboundHandler.sendMessage(request);

            //注册连接事件
            channelFuture.addListener((ChannelFutureListener) future -> {
                //如果连接成功
                if (future.isSuccess()) {
                    log.info("客户端[" + channelFuture.channel().localAddress().toString() + "]已连接...");
                    //clientChannel = channelFuture.channel();
                }
                //如果连接失败，尝试重新连接
                else {
                    log.info("客户端[" + channelFuture.channel().localAddress().toString() + "]连接失败，重新连接中...");
                    future.channel().close();
                    bootstrap.connect(uri.getHost(), uri.getPort());
                }
            });

            //注册关闭事件
            channelFuture.channel().closeFuture().addListener(cfl -> {
                //close();
                log.info("客户端[" + channelFuture.channel().localAddress().toString() + "]已断开...");
            }).sync();

            //channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
        return httpInboundHandler.getResponse();
    }
}
```

**netty channel通道初始化类HttpInboundInitializer**

```java
package com.nerotomato.rpcfx.client.initializer;

import com.nerotomato.rpcfx.client.handler.HttpInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

/**
 * Created by nero on 2021/6/3.
 */
public class HttpInboundInitializer extends ChannelInitializer<SocketChannel> {
    private HttpInboundHandler httpInboundHandler;

    public HttpInboundInitializer(HttpInboundHandler httpInboundHandler) {
        this.httpInboundHandler = httpInboundHandler;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        // 客户端接收到的是httpResponse响应，所以要使用HttpResponseDecoder进行解码
        p.addLast(new HttpResponseDecoder());
        // 客户端发送的是httpRequest，所以要使用HttpRequestEncoder进行编码
        p.addLast(new HttpRequestEncoder());
        //p.addLast(new HttpObjectAggregator(1024 * 1024));
        //p.addLast(new HttpContentCompressor());
        p.addLast(httpInboundHandler);
    }
}
```

**业务请求响应处理类HttpInboundHandler**

```java
package com.nerotomato.rpcfx.client.handler;

import com.alibaba.fastjson.JSON;
import com.nerotomato.rpcfx.api.RpcfxResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Created by nero on 2021/6/3.
 */
@Slf4j
public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private ChannelHandlerContext ctx;
    private ChannelPromise promise;
    private RpcfxResponse rpcfxResponse;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
    }

    /**
     * Netty的ByteBuf支持缓存池技术，目的是提高频繁创建的ByteBuf的利用率，避免系统内存的浪费。
     * 池子里ByteBuf的回收通过引用计数的方式。每次调用retain()方法，引用+1，每次调用release()方法，引用计数-1。刚创建的ByteBuf计数为1。
     * 如果引用计数=0，
     * （1）如果是Pooled池化的ByteBuf，继续放入可以重新分配的池子里。
     * （2）如果是Unpooled未池化的，回收分两种情况：如果是堆结构，JVM垃圾回收。如果是Direct类型，调用本地方法(unsafe.freeMemory)释放内存。
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpContent) {
            System.out.println("msg -> " + msg);
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf content = httpContent.content();
            byte[] bytes = new byte[content.readableBytes()];
            content.readBytes(bytes);
            String result = new String(bytes);
            if (null != result && !result.isEmpty()) {
                rpcfxResponse = JSON.parseObject(result, RpcfxResponse.class);
                //content.release(); //引用计数-1
                System.out.println("Response message is : " + result);
                promise.setSuccess();
            }

        } else {
            ctx.fireChannelRead(msg);
        }

        if (msg instanceof DefaultHttpResponse) {
            DefaultHttpResponse defaultHttpResponse = (DefaultHttpResponse) msg;
            String result = defaultHttpResponse.toString();
        } else {
            ctx.fireChannelRead(msg);
        }

    }

    public synchronized ChannelPromise sendMessage(Object message) {
        while (ctx == null) {
            try {
                TimeUnit.MILLISECONDS.sleep(1);
                //logger.error("等待ChannelHandlerContext实例化");
                log.info("等待ChannelHandlerContext实例化......");
            } catch (InterruptedException e) {
                log.error("等待ChannelHandlerContext实例化过程中出错", e);
            }
        }
        promise = ctx.newPromise();
        ctx.writeAndFlush(message);
        return promise;
    }

    public RpcfxResponse getResponse() {
        return rpcfxResponse;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
```