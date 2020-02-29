package cn.t.tool.netproxytool.socks5.server.handler;

import cn.t.tool.netproxytool.component.ChannelContextMessageSender;
import cn.t.tool.netproxytool.component.MessageSender;
import cn.t.tool.netproxytool.event.ProxyBuildResultListener;
import cn.t.tool.netproxytool.socks5.constants.Socks5CmdExecutionStatus;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

/**
 * 抓取消息处理器处理器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-22 20:54
 **/
@Slf4j
public class FetchMessageHandler extends ForwardingMessageHandler {

    private ProxyBuildResultListener proxyBuildResultListener;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("[{}]: 连接成功, 回调代客户端听器", ctx.channel().remoteAddress());
        proxyBuildResultListener.handle(Socks5CmdExecutionStatus.SUCCEEDED.value, new ChannelContextMessageSender(ctx));
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        ctx.connect(remoteAddress, localAddress, promise.addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                //连接失败处理
                log.info("[{}]: 连接失败, 回调客户端监听器", ctx.channel().remoteAddress());
                proxyBuildResultListener.handle(Socks5CmdExecutionStatus.CONNECTION_REFUSED.value, new ChannelContextMessageSender(ctx));
            }
        }));
    }

    public FetchMessageHandler(MessageSender messageSender, ProxyBuildResultListener proxyBuildResultListener) {
        super(messageSender);
        this.proxyBuildResultListener = proxyBuildResultListener;
    }
}