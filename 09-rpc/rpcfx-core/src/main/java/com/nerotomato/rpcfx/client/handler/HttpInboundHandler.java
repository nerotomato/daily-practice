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
            //System.out.println("response -> " + result);
            //rpcfxResponse = JSON.parseObject(result, RpcfxResponse.class);
            //System.out.println("Response message is : " + result);
            //promise.setSuccess();
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
