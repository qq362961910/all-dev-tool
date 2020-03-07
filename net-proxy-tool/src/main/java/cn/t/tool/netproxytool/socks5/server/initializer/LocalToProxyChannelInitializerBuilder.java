package cn.t.tool.netproxytool.socks5.server.initializer;

import cn.t.tool.netproxytool.socks5.constants.Socks5ServerConfig;
import cn.t.tool.netproxytool.socks5.server.analyse.NegotiateRequestAnalyse;
import cn.t.tool.netproxytool.socks5.server.encoder.ServerCmdResponseEncoder;
import cn.t.tool.netproxytool.socks5.server.encoder.ServerNegotiateResponseEncoder;
import cn.t.tool.netproxytool.socks5.server.handler.CmdRequestHandler;
import cn.t.tool.netproxytool.socks5.server.handler.NegotiateRequestHandler;
import cn.t.tool.nettytool.initializer.NettyChannelInitializerBuilder;

/**
 * @author yj
 * @since 2020-01-12 16:31
 **/
public class LocalToProxyChannelInitializerBuilder extends NettyChannelInitializerBuilder {

    public LocalToProxyChannelInitializerBuilder() {
        setLoggingHandlerLogLevel(Socks5ServerConfig.LOGGING_HANDLER_LOGGER_LEVEL);
        setIdleState(Socks5ServerConfig.SOCKS5_PROXY_READ_TIME_OUT_IN_SECONDS, Socks5ServerConfig.SOCKS5_PROXY_WRITE_TIME_OUT_IN_SECONDS, Socks5ServerConfig.SOCKS5_PROXY_ALL_IDLE_TIME_OUT_IN_SECONDS);
        setByteBufAnalyserSupplier(NegotiateRequestAnalyse::new);
        addEncoderListsSupplier(ServerNegotiateResponseEncoder::new);
        addEncoderListsSupplier(ServerCmdResponseEncoder::new);
        addChannelHandlerSupplier(NegotiateRequestHandler::new);
        addChannelHandlerSupplier(CmdRequestHandler::new);
    }
}
