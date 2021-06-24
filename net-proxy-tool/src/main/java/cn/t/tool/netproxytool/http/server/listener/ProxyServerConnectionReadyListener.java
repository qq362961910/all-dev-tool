package cn.t.tool.netproxytool.http.server.listener;

import cn.t.tool.netproxytool.handler.ForwardingMessageHandler;
import cn.t.tool.netproxytool.http.server.handler.HttpProxyServerHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * https代理结果监听器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-27 15:42
 **/
@Slf4j
public class ProxyServerConnectionReadyListener implements ChannelFutureListener {

    private final ChannelHandlerContext localChannelHandlerContext;
    private final ChannelHandlerContext remoteChannelHandlerContext;
    private final String clientName;

    @Override
    public void operationComplete(ChannelFuture future) {
        if(future.isSuccess()) {
            log.info("{}: 代理连接已就位", clientName);
            //已经通知客户端代理成功, 切换handler
            ChannelPipeline channelPipeline = localChannelHandlerContext.channel().pipeline();
            channelPipeline.remove(HttpResponseEncoder.class);
            channelPipeline.remove(HttpRequestDecoder.class);
            channelPipeline.remove(HttpObjectAggregator.class);
            channelPipeline.remove(HttpProxyServerHandler.class);
            channelPipeline.addLast("proxy-forwarding-handler", new ForwardingMessageHandler(remoteChannelHandlerContext));
        } else {
            log.error("{}: 代理请求发送失败, 即将关闭连接, 失败原因: {}", clientName, future.cause().getMessage());
            localChannelHandlerContext.close();
        }
    }

    public ProxyServerConnectionReadyListener(ChannelHandlerContext localChannelHandlerContext, ChannelHandlerContext remoteChannelHandlerContext, String clientName) {
        this.localChannelHandlerContext = localChannelHandlerContext;
        this.remoteChannelHandlerContext = remoteChannelHandlerContext;
        this.clientName = clientName;
    }
}
