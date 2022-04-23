package com.can.rpc.remoting.netty;

import com.can.rpc.remoting.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.List;

/**
 * 这里不做具体的协议
 * 对于发送端 发送的数据以特定的协议格式进行发送
 * 接受端  把请求的网络字节数据 转成java对象
 * @author ccc
 */
public class NettyCodec extends ChannelDuplexHandler {

    private Codec codec;

    public NettyCodec(Codec codec) {
        this.codec = codec;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.fireChannelRead(msg);
//        ByteBuf byteBuf = (ByteBuf) msg;
//        byte[] data = new byte[byteBuf.readableBytes()];
//        byteBuf.readBytes(data);
//
//        List<Object> out = codec.decode(data);
//
//        for (Object o : out) {
//            ctx.fireChannelRead(o);
//        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        byte[] message = codec.encode(msg);
        super.write(ctx, Unpooled.wrappedBuffer(message), promise);
    }
}
