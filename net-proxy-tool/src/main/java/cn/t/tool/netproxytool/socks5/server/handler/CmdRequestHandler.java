package cn.t.tool.netproxytool.socks5.server.handler;

import cn.t.tool.netproxytool.event.ProxyBuildResultListener;
import cn.t.tool.netproxytool.exception.ProxyException;
import cn.t.tool.netproxytool.socks5.constants.Socks5AddressType;
import cn.t.tool.netproxytool.socks5.constants.Socks5Cmd;
import cn.t.tool.netproxytool.socks5.constants.Socks5CmdExecutionStatus;
import cn.t.tool.netproxytool.socks5.constants.Socks5ServerDaemonConfig;
import cn.t.tool.netproxytool.socks5.model.CmdRequest;
import cn.t.tool.netproxytool.socks5.model.CmdResponse;
import cn.t.tool.netproxytool.socks5.server.UserRepository;
import cn.t.tool.netproxytool.socks5.server.initializer.ProxyToRemoteChannelInitializerBuilder;
import cn.t.tool.netproxytool.socks5.server.listener.Socks5ProxyForwardingResultListener;
import cn.t.tool.netproxytool.util.ThreadUtil;
import cn.t.tool.nettytool.aware.NettyTcpDecoderAware;
import cn.t.tool.nettytool.client.NettyTcpClient;
import cn.t.tool.nettytool.decoder.NettyTcpDecoder;
import cn.t.tool.nettytool.initializer.NettyChannelInitializer;
import cn.t.util.common.StringUtil;
import cn.t.util.security.message.base64.Base64Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 命令请求处理器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-20 22:30
 **/
@Slf4j
public class CmdRequestHandler extends SimpleChannelInboundHandler<CmdRequest> implements NettyTcpDecoderAware {

    private NettyTcpDecoder nettyTcpDecoder;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CmdRequest msg) {
        String username = ctx.channel().attr(Socks5ServerDaemonConfig.CHANNEL_USERNAME).get();
        String security = null;
        if(!StringUtil.isEmpty(username)) {
            security = UserRepository.getUserSecurity(username);
        }
        byte[] securityBytes = StringUtil.isEmpty(security) ? null : Base64Util.decode(security.getBytes());
        if(msg.getRequestSocks5Cmd() == Socks5Cmd.CONNECT) {
            InetSocketAddress remoteAddress = (InetSocketAddress)ctx.channel().remoteAddress();
            String targetHost = new String(msg.getTargetAddress());
            int targetPort = msg.getTargetPort();
            log.info("[{}]: [{}], 地址类型: {}, 地址: {}， 目标端口: {}", remoteAddress, Socks5Cmd.CONNECT, msg.getSocks5AddressType(), targetHost, targetPort);
            ProxyBuildResultListener proxyBuildResultListener = (status, remoteChannelHandlerContext) -> {
                Socks5CmdExecutionStatus socks5CmdExecutionStatus = Socks5CmdExecutionStatus.getSocks5CmdExecutionStatus(status);
                CmdResponse cmdResponse = new CmdResponse();
                cmdResponse.setVersion(msg.getVersion());
                cmdResponse.setExecutionStatus(socks5CmdExecutionStatus.value);
                cmdResponse.setRsv((byte)0);
                cmdResponse.setSocks5AddressType(Socks5AddressType.IPV4.value);
                cmdResponse.setTargetAddress(Socks5ServerDaemonConfig.SERVER_HOST_BYTES);
                cmdResponse.setTargetPort(Socks5ServerDaemonConfig.SERVER_PORT);
                if(Socks5CmdExecutionStatus.SUCCEEDED.value == status) {
                    log.info("[{}]: 代理客户端成功, remote: {}:{}", remoteAddress, targetHost, targetPort);
                    ChannelPromise promise = ctx.newPromise();
                    promise.addListener(new Socks5ProxyForwardingResultListener(ctx, remoteChannelHandlerContext, targetHost, targetPort, securityBytes, nettyTcpDecoder));
                    ctx.writeAndFlush(cmdResponse, promise);
                } else {
                    log.error("[{}]: 代理客户端失败, remote: {}:{}", remoteAddress, targetHost, targetPort);
                    ctx.writeAndFlush(cmdResponse);
                }
            };
            String clientName = remoteAddress.getHostString() + ":" + remoteAddress.getPort() + " -> " + targetHost + ":" + targetPort;
            NettyChannelInitializer channelInitializer = new ProxyToRemoteChannelInitializerBuilder(ctx, proxyBuildResultListener, securityBytes).build();
            NettyTcpClient nettyTcpClient = new NettyTcpClient(clientName, targetHost, targetPort, channelInitializer);
            ThreadUtil.submitProxyTask(() -> nettyTcpClient.start(null));
        } else {
            throw new ProxyException("未实现的命令处理: " + msg.getRequestSocks5Cmd());
        }
    }

    @Override
    public void setNettyTcpDecoder(NettyTcpDecoder nettyTcpDecoder) {
        this.nettyTcpDecoder = nettyTcpDecoder;
    }
}
