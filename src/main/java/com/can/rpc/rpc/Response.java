package com.can.rpc.rpc;

/**
 * @author ccc
 */
public class Response {

    public static final byte SUCCESS = 20;

    public static final byte ERROR = 50;

    private long requsetId; //请求中的messageId

    private byte status; // 20 success

    //todo 优化 成object类型 解决序列化LinkedHashMap
    private SyncResult content; //响应内容

    private String errInfo;

    @Override
    public String toString() {
        return "Response{" +
                "requsetId=" + requsetId +
                ", status=" + status +
                ", content=" + content +
                ", errInfo=" + errInfo +
                '}';
    }

    public long getRequsetId() {
        return requsetId;
    }

    public void setRequsetId(long requsetId) {
        this.requsetId = requsetId;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public SyncResult getContent() {
        return content;
    }

    public void setContent(SyncResult content) {
        this.content = content;
    }

    public String getErrInfo() {
        return errInfo;
    }

    public void setErrInfo(String errInfo) {
        this.errInfo = errInfo;
    }
}
