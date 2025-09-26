package com.it.weblogclient.domain.msg;

import java.util.List;

public class PendingBatch {
    private final String batchId;
    private final List<Integer> idList;
    private final String jsonStr;
    private final long sendTime;
    private int retryCount;

    public PendingBatch(String batchId, List<Integer> idList, String jsonStr) {
        this.batchId = batchId;
        this.idList = idList;
        this.jsonStr = jsonStr;
        this.sendTime = System.currentTimeMillis();
        this.retryCount = 0;
    }

    public PendingBatch(String batchId, List<Integer> idList, String jsonStr, int retryCount) {
        this.batchId = batchId;
        this.idList = idList;
        this.jsonStr = jsonStr;
        this.sendTime = System.currentTimeMillis();
        this.retryCount = retryCount;
    }

    public List<Integer> getIdList() {
        return idList;
    }

    public long getSendTime() {
        return sendTime;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getJsonStr() {
        return jsonStr;
    }

    public void incrementRetry(){
        retryCount++;
    }

    public String getBatchId() {
        return batchId;
    }
}
