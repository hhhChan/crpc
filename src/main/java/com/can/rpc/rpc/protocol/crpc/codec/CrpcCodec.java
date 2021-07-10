package com.can.rpc.rpc.protocol.crpc.codec;

import com.can.rpc.common.serialize.Serialization;
import com.can.rpc.common.tools.ByteUtil;
import com.can.rpc.remoting.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

public class CrpcCodec implements Codec {
    public final static byte[] MAGIC = new  byte[]{(byte) 0xca, (byte) 0xc1};

    //协议头部长度
    public static final int HEADER_LEN = 6;

    //用来临时保留没有处理过的请求报文
    ByteBuf tempMsg = Unpooled.buffer();

    Serialization serialization;

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }

    public Serialization getSerialization() {
        return this.serialization;
    }

    Class decodeType;

    public Class getDecodeType() {
        return decodeType;
    }

    public void setDecodeType(Class decodeType) {
        this.decodeType = decodeType;
    }

    /**
     * 客户端 -- 编码 -- rpcinvocation
     * 服务端 -- 编码 -- response
     * @param msg
     * @return
     * @throws Exception
     */
    @Override
    public byte[] encode(Object msg) throws Exception {
        byte[] responseBody = (byte[]) msg;
        // 构建header
        ByteBuf requestBuffer = Unpooled.buffer();
        requestBuffer.writeByte(0xca);
        requestBuffer.writeByte(0xc1);
        requestBuffer.writeBytes(ByteUtil.int2bytes(responseBody.length));
        requestBuffer.writeBytes(responseBody);

        byte[] result = new byte[requestBuffer.readableBytes()];
        requestBuffer.readBytes(result);
        return result;
    }

    /**
     * 服务端 - 解码结果 是Rpcinvocation 对象集合
     * 客户端 - 解码结果 -- response对象集合
     * @param msg
     * @return
     * @throws Exception
     */
    @Override
    public List<Object> decode(byte[] msg) throws Exception {
        List<Object> out = new ArrayList<>();

        //1 解析(解析头部，取出数据，封装成invocation)
        //1.1合并成报文
        ByteBuf message = Unpooled.buffer();
        int tmpMsgSize = tempMsg.readableBytes();
        //如果上次暂存有余下的请求报文，则合并
        if (tmpMsgSize > 0) {
            message.writeBytes(tempMsg);
            message.writeBytes(msg);
            System.out.println("合并：上一数据包余下的长度为：" + tmpMsgSize + ",合并后长度为:" + message.readableBytes());
        } else {
            message.writeBytes(msg);
        }

        while(true) {
            // 如果数据太少，不够一个头部，待会处理
            if (HEADER_LEN > message.readableBytes()) {
                tempMsg.clear();
                tempMsg.writeBytes(message);
                return out;
            }

            //1.2解析数据
            //1.2.1检查关键字
            byte[] magic = new byte[2];
            message.readBytes(magic);
            while (true) {
                // 如果不符合关键字，则一直读取到有正常的关键字为止。
                if (MAGIC[0] != magic[0] || magic[1] != MAGIC[1]) {
                    // 所有数据读完都没发现正确的头，算了.. 等下次数据
                    if (message.readableBytes() == 0) {
                        tempMsg.clear();
                        tempMsg.writeByte(magic[1]);
                        return out;
                    }
                    magic[0] = magic[1];
                    magic[1] = message.readByte();
                } else {
                    break;
                }
            }

            byte[] lengthBytes = new byte[4];
            message.readBytes(lengthBytes);
            int length = ByteUtil.Byte2Int_BE(lengthBytes);
            // 1.2.2 读取body
            // 如果body没传输完，先不处理
            if (message.readableBytes() < length) {
                tempMsg.clear();
                tempMsg.writeBytes(magic);
                tempMsg.writeBytes(lengthBytes);
                tempMsg.writeBytes(message);
                return out;
            }
            byte[] body = new byte[length];
            message.readBytes(body);
            // 序列化
            Object o = getSerialization().deserialize(body, decodeType);
            out.add(o);
        }
    }

    @Override
    public Codec createInstance() {
        CrpcCodec crpcCodec = new CrpcCodec();
        crpcCodec.setDecodeType(this.decodeType);
        crpcCodec.setSerialization(this.serialization);
        return crpcCodec;
    }
}
