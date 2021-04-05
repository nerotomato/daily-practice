package outbound.okhttp;

import filter.MyHttpRequestFilter;
import filter.MyHttpResponseFilter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import okhttp3.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import router.HttpEndpointRouter;
import router.RandomHttpEndpointRouter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class MyOkhttpOutboundHandler {
    private List<String> backendUrls;
    private MyHttpResponseFilter responseFilter = new MyHttpResponseFilter();
    private HttpEndpointRouter router = new RandomHttpEndpointRouter();
    private OkHttpClient okHttpClient = null;

    public MyOkhttpOutboundHandler(List<String> backendsServers) {
        this.backendUrls = backendsServers;
        this.backendUrls = backendsServers.stream().map(this::formatUrl).collect(Collectors.toList());
        okHttpClient = new OkHttpClient();
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

        Request request = new Request.Builder()
                //请求地址
                .url(url)
                //设置请求头
                .addHeader("author",fullRequest.headers().get("author"))
                .build();

        /**
         * 请求的发送有两种形式：
         * 一种是直接同步执行，阻塞调用线程，直接返回结果；（不推荐，Android中可能阻塞UI线程）
         * 另一种是通过队列异步执行，不阻塞调用线程，通过回调方法返回结果。如下所示：
         * */
        //Response response = null;
        //同步执行：如果返回null,代表超时或没有网络连接
        //response = okHttpClient.newCall(request).execute();
        /* if (response != null) {
            long contentLength = response.body().contentLength();
            String message = response.body().string();
            System.out.println("响应内容长度为：" + contentLength);
            System.out.println("响应内容为：" + message);
        }*/

        //将request封装为Call
        Call call = okHttpClient.newCall(request);
        //异步调用,并设置回调函数
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response != null) {
                    //处理响应信息
                    handleResponse(fullRequest, ctx, response);
                }
            }
        });
    }

    private void handleResponse(FullHttpRequest fullRequest, ChannelHandlerContext ctx, Response response) {
        FullHttpResponse fullHttpResponse = null;
        try {
            byte[] body = response.body().bytes();
            fullHttpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(body));

            fullHttpResponse.headers().set("Content-Type", "application/json");
            fullHttpResponse.headers().setInt("Content-Length", Integer.parseInt(String.valueOf(response.body().contentLength())));
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
