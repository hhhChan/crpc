package com.can.rpc.common.tools;

import java.nio.ByteBuffer;

/**
 * @author ccc
 */
public class ByteUtil {

    public static byte[] short2bytes(short v) {
        byte[] b = new byte[4];
        b[1] = (byte) v;
        b[0] = (byte) (v >>> 8);
        return b;
    }

    public static byte[] int2bytes(int v) {
        byte[] b = new byte[4];
        b[3] = (byte) v;
        b[2] = (byte) (v >>> 8);
        b[1] = (byte) (v >>> 16);
        b[0] = (byte) (v >>> 24);
        return b;
    }

    public static byte[] long2bytes(long v) {
        byte[] b = new byte[8];
        b[7] = (byte) v;
        b[6] = (byte) (v >>> 8);
        b[5] = (byte) (v >>> 16);
        b[4] = (byte) (v >>> 24);
        b[3] = (byte) (v >>> 32);
        b[2] = (byte) (v >>> 40);
        b[1] = (byte) (v >>> 48);
        b[0] = (byte) (v >>> 56);
        return b;
    }

    public static String bytes2HexString(byte[] v) {
        if (v == null || v.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : v) {
            String temp = Integer.toHexString(Byte.toUnsignedInt(b));
            if (temp.length() < 2) {
                sb.append(0);
            }
            sb.append(temp);
        }
        return sb.toString();
    }

    //数组长度至少为4，按大端方式转换，即传入的bytes是大端的，按这个规律组织成int
    public static int bytes2Int_BE(byte[] v) {
        if (v.length < 4) {
            return -1;
        }
        int iRst = (v[0] & 0xFF) << 24;
        iRst |= (v[1] & 0xFF) << 16;
        iRst |= (v[2] & 0xFF) << 8;
        iRst |= v[3] & 0xFF;
        return iRst;
    }

    public static long bytes2Long(byte[] v) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(v, 0, v.length);
        buffer.flip();
        return buffer.getLong();
    }
}
