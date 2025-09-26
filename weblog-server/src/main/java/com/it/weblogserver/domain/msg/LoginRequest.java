package com.it.weblogserver.domain.msg;

import com.it.weblogserver.domain.BasicMsg;
import com.it.weblogserver.utils.SlidingWindow;

public class LoginRequest extends BasicMsg {

    private String appKey;

    private int timeMillisPerSlice;
    /**
     * 共有多少个时间片（即窗口长度）
     */
    private int windowSize;
    /**
     * 在一个完整窗口期内允许通过的最大阈值
     */
    private int threshold;

    public LoginRequest(String appKey, int timeMillisPerSlice, int windowSize, int threshold, String type){
        this.timeMillisPerSlice = timeMillisPerSlice;
        this.windowSize = windowSize;
        this.threshold = threshold;
        this.appKey = appKey;
        this.type = type;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public int getTimeMillisPerSlice() {
        return timeMillisPerSlice;
    }

    public void setTimeMillisPerSlice(int timeMillisPerSlice) {
        this.timeMillisPerSlice = timeMillisPerSlice;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}
