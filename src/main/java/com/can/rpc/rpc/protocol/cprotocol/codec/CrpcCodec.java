package com.can.rpc.rpc.protocol.cprotocol.codec;

import com.can.rpc.common.serialize.Serialization;
import com.can.rpc.common.tools.ByteUtil;
import com.can.rpc.remoting.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ccc
 */
public class CrpcCodec implements Codec {

    public final static byte[] MAGIC = new byte[]{(byte)0xcc, (byte)0xcc};

    //协议长度
    public final static int HEADER_LEN = 6;

    //保存尚未处理完的数据
    private ByteBuf temptMsg = Unpooled.buffer();

    private Serialization serialization;

    private Class decodeType;

    public Class getDecodeType() {
        return decodeType;
    }

    public void setDecodeType(Class decodeType) {
        this.decodeType = decodeType;
    }

    public Serialization getSerialization() {
        return serialization;
    }

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }



    @Override
    public byte[] encode(Object msg) throws Exception {
        byte[] responseBody = getSerialization().serialize(msg);
        ByteBuf requestBuf = Unpooled.buffer();
        requestBuf.writeByte(0xcc);
        requestBuf.writeByte(0xcc);
        requestBuf.writeBytes(ByteUtil.int2bytes(responseBody.length));
        requestBuf.writeBytes(responseBody);

        byte[] result = new byte[requestBuf.readableBytes()];
        requestBuf.readBytes(result);
        return result;
    }

    @Override
    public Object decode(byte[] msg) throws Exception {
        return this.getSerialization().deserialize(msg, decodeType);
//        List<Object> out = new ArrayList<>();
//
//        ByteBuf buffer = Unpooled.buffer();
//        int tempSize = temptMsg.readableBytes();
//        if (tempSize > 0) {
//            buffer.writeBytes(temptMsg);
//            buffer.writeBytes(msg);
//        } else {
//            buffer.writeBytes(msg);
//        }
//
//        while (true) {
//            if (HEADER_LEN >= buffer.readableBytes()) {
//                temptMsg.clear();
//                temptMsg.writeBytes(buffer);
//                buffer.release();
//                return out;
//            }
//
//            byte[] magic = new byte[2];
//            buffer.readBytes(magic);
//
//            while (true) {
//                if (magic[0] != MAGIC[0] || magic[1] != MAGIC[1]) {
//                    if (buffer.readableBytes() == 0) {
//                        temptMsg.clear();
//                        temptMsg.writeByte(magic[1]);
//                        buffer.release();
//                        return out;
//                    }
//                    magic[0] = magic[1];
//                    magic[1] = buffer.readByte();
//                } else {
//                    break;
//                }
//            }
//            byte[] lengthBytes = new byte[4];
//            buffer.readBytes(lengthBytes);
//            int length = ByteUtil.bytes2Int_BE(lengthBytes);
//
//            if (buffer.readableBytes() < length) {
//                temptMsg.clear();
//                temptMsg.writeBytes(MAGIC);
//                temptMsg.writeBytes(lengthBytes);
//                temptMsg.writeBytes(buffer);
//                buffer.release();
//                return out;
//            }
//
//            byte[] body = new byte[length];
//            //这里是读
//            buffer.readBytes(body);
//            Object o = this.getSerialization().deserialize(body, decodeType);
//            out.add(o);
//        }
    }

    @Override
    public Codec createIstance() {
        CrpcCodec codec = new CrpcCodec();
        codec.setDecodeType(decodeType);
        codec.setSerialization(serialization);
        return codec;
    }
}
