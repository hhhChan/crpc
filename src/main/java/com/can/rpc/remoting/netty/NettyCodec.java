package com.can.rpc.remoting.netty;

import com.can.rpc.remoting.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.List;

public class NettyCodec extends ChannelDuplexHandler {
    private Codec codec;

    public NettyCodec(Codec codec) {
        this.codec = codec;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //读数据
        ByteBuf buf = (ByteBuf) msg;
        byte[] dataBytes = new byte[buf.readableBytes()];
        buf.readBytes(dataBytes);
        //格式转
        List<Object> out = codec.decode(dataBytes);
        // 处理器继续处理
        for (Object o : out) {
            ctx.fireChannelRead(o);
        }
        System.out.println("内容" + msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        byte[] encode = codec.encode(msg);
        super.write(ctx, Unpooled.wrappedBuffer(encode), promise);
    }
}
