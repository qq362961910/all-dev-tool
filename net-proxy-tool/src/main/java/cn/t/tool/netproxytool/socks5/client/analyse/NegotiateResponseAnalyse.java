package cn.t.tool.netproxytool.socks5.client.analyse;

import cn.t.tool.netproxytool.socks5.model.NegotiateResponse;
import cn.t.tool.nettytool.analyser.ByteBufAnalyser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * NegotiateResponse解析器
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-03-14 20:42
 **/
public class NegotiateResponseAnalyse extends ByteBufAnalyser {

    @Override
    public Object analyse(ByteBuf byteBuf, ChannelHandlerContext channelHandlerContext) {
        NegotiateResponse negotiateResponse = new NegotiateResponse();
        negotiateResponse.setVersion(byteBuf.readByte());
        negotiateResponse.setSocks5Method(byteBuf.readByte());
        return negotiateResponse;
    }
}
