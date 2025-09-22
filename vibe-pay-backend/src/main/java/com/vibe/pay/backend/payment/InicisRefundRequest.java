package com.vibe.pay.backend.payment;

public class InicisRefundRequest {
    private String mid;
    private String type;
    private String timestamp;
    private String clientIp;
    private String hashData;
    private RefundData data;

    // Constructors
    public InicisRefundRequest() {
        this.type = "refund"; // 고정값
    }

    // Inner class for data
    public static class RefundData {
        private String tid;
        private String msg;

        public RefundData() {}

        public RefundData(String tid, String msg) {
            this.tid = tid;
            this.msg = msg;
        }

        public String getTid() {
            return tid;
        }

        public void setTid(String tid) {
            this.tid = tid;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        @Override
        public String toString() {
            return "RefundData{" +
                    "tid='" + tid + '\'' +
                    ", msg='" + msg + '\'' +
                    '}';
        }
    }

    // Getters and Setters
    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getHashData() {
        return hashData;
    }

    public void setHashData(String hashData) {
        this.hashData = hashData;
    }

    public RefundData getData() {
        return data;
    }

    public void setData(RefundData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "InicisRefundRequest{" +
                "mid='" + mid + '\'' +
                ", type='" + type + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", hashData='" + hashData + '\'' +
                ", data=" + data +
                '}';
    }
}