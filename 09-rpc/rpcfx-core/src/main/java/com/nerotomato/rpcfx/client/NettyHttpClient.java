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
