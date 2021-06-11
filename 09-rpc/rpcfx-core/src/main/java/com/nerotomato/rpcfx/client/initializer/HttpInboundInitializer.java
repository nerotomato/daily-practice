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
