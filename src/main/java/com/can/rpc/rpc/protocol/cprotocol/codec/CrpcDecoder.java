package com.can.rpc.rpc.protocol.cprotocol.codec;

import com.can.rpc.common.serialize.Serialization;
import com.can.rpc.remoting.Codec;
import com.can.rpc.rpc.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

/**
 * @author ccc
 */
public class CrpcDecoder extends ByteToMessageDecoder {

    private Codec codec;

    public CrpcDecoder(Codec codec) {
        this.codec = codec;
    }

    public final static byte[] MAGIC = new byte[]{(byte)0xcc, (byte)0xcc};


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            if (in.readableBytes () < 6) {
                return;
            }

            in.markReaderIndex ();

            byte[] byte_len = new byte[4];
            byte[] magic = new byte[2];
            in.readBytes(magic);
            in.readBytes(byte_len);

            int length = decodeFrameSize(byte_len);

            if (length < 0) {
                throw new CorruptedFrameException("negative length: " + length);
            }

            if (magic[0] != MAGIC[0] || magic[1] != MAGIC[1] || in.readableBytes () < length) {
                //reset the readerIndex
                in.resetReaderIndex ();
                return;
            }

            byte[] content = new byte[length];
            in.readBytes(content);

            out.add(codec.decode(content));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static final int decodeFrameSize(byte[] buf) {
        return (buf[0] & 255) << 24 | (buf[1] & 255) << 16 | (buf[2] & 255) << 8 | buf[3] & 255;
    }
}
