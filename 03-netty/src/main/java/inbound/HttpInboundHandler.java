package inbound;

import filter.MyHttpRequestFilter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outbound.httpclient.MyHttpOutBoundHandler;
import outbound.okhttp.MyOkhttpOutboundHandler;

import java.util.List;

public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpInboundHandler.class);
    private final List<String> proxyServer;

    private MyHttpRequestFilter myHttpRequestFilter = new MyHttpRequestFilter();
    //private MyHttpOutBoundHandler myHandler;
    private MyOkhttpOutboundHandler myHandler;
    public HttpInboundHandler(List<String> proxyServer) {
        this.proxyServer = proxyServer;
        //this.myHandler = new MyHttpOutBoundHandler(this.proxyServer);
        this.myHandler = new MyOkhttpOutboundHandler(this.proxyServer);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            //logger.info("channelRead流量接口请求开始，时间为{}", startTime);
            FullHttpRequest fullRequest = (FullHttpRequest) msg;
            myHandler.handle(fullRequest, ctx, myHttpRequestFilter);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
