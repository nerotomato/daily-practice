package outbound.httpclient;

import filter.MyHttpRequestFilter;
import filter.MyHttpResponseFilter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import router.HttpEndpointRouter;
import router.RandomHttpEndpointRouter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * httpclient访问后台服务
 * Created by nero on 2021/4/5.
 */
public class MyHttpOutBoundHandler {

    private CloseableHttpClient httpClient;
    private List<String> backendUrls;
    private MyHttpResponseFilter responseFilter = new MyHttpResponseFilter();
    private HttpEndpointRouter router = new RandomHttpEndpointRouter();

    public MyHttpOutBoundHandler(List<String> backendsServers) {
        this.backendUrls = backendsServers;
        this.backendUrls = backendsServers.stream().map(this::formatUrl).collect(Collectors.toList());
        httpClient = HttpClientBuilder.create().build();
    }

    public void handle(FullHttpRequest fullRequest, ChannelHandlerContext ctx, MyHttpRequestFilter myHttpRequestFilter) {
        String backendUrl = router.route(this.backendUrls);
        final String url = backendUrl + fullRequest.uri();
        //执行请求过滤器，添加请求头
        myHttpRequestFilter.filter(fullRequest, ctx);
        try {
            executeGet(fullRequest, ctx, url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeGet(FullHttpRequest fullRequest, ChannelHandlerContext ctx, String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
        //获取request过滤器添加的请求头
        httpGet.setHeader("author", fullRequest.headers().get("author"));
        //调用HttpClient获取响应
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            //处理响应
            handleResponse(fullRequest, ctx, response);
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

    private void handleResponse(FullHttpRequest fullRequest, ChannelHandlerContext ctx, CloseableHttpResponse httpResponse) {
        FullHttpResponse fullHttpResponse = null;
        try {
            byte[] body = EntityUtils.toByteArray(httpResponse.getEntity());
            fullHttpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(body));

            fullHttpResponse.headers().set("Content-Type", "application/json");
            fullHttpResponse.headers().setInt("Content-Length", Integer.parseInt(httpResponse.getFirstHeader("Content-Length").getValue()));
            //调用响应过滤器
            responseFilter.filter(fullHttpResponse);
        } catch (Exception e) {
            e.printStackTrace();
            fullHttpResponse = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
            exceptionCaught(ctx, e);
        } finally {
            if (fullRequest != null) {
                if (!HttpUtil.isKeepAlive(fullRequest)) {
                    ctx.write(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
                } else {
                    ctx.write(fullHttpResponse);
                }
            }
            ctx.flush();
        }
    }

    private String formatUrl(String backend) {
        return backend.endsWith("/") ? backend.substring(0, backend.length() - 1) : backend;
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
