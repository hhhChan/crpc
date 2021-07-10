package com.can.rpc.common.tools;

import java.nio.ByteBuffer;

public class ByteUtil {
    public static byte[] short2bytes(short v) {
        byte[] b = new byte[2];
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

    public static String bytes2HexString(byte[] bs) {
        if (bs == null && bs.length == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        String tmp = null;
        for (byte b : bs) {
            tmp = Integer.toHexString(Byte.toUnsignedInt(b));
            if (tmp.length() < 2) {
                sb.append(0);
            }
            sb.append(tmp);
        }

        return sb.toString();
    }

    public static int Byte2Int_BE(byte[] bytes) {
        if (bytes.length < 4) {
            return -1;
        }
        int res = (bytes[0] << 24) & 0xFF;
        res |= (bytes[1] << 16) & 0xFF;
        res |= (bytes[2] << 8) & 0xFF;
        res |= bytes[3] &0xFF;

        return res;
    }

    public static long Byte2long(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();
        return buffer.getLong();
    }
}
