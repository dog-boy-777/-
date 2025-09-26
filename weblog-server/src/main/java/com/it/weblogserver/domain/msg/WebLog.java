package com.it.weblogserver.domain.msg;

import com.it.weblogserver.domain.BasicMsg;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "web_log")
public class WebLog extends BasicMsg {

    @Id
    private int id;
    private long heapMemoryUsed;
    private long heapMemoryMax;
    private String errorMessage;
    private String errorStack;
    private LocalDateTime occurTime;
    private Integer sendStatus;
    private String appKey;

    public WebLog(){}


    public WebLog(long heapMemoryUsed, long heapMemoryMax, String errorMessage, String errorStack, LocalDateTime occurTime, String type){
        this.heapMemoryUsed = heapMemoryUsed;
        this.heapMemoryMax = heapMemoryMax;
        this.errorMessage = errorMessage;
        this.errorStack = errorStack;
        this.occurTime = occurTime;
        this.sendStatus = 0;
        this.type = type;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setErrorStack(String errorStack) {
        this.errorStack = errorStack;
    }

    public void setHeapMemoryMax(long heapMemoryMax) {
        this.heapMemoryMax = heapMemoryMax;
    }

    public void setHeapMemoryUsed(long heapMemoryUsed) {
        this.heapMemoryUsed = heapMemoryUsed;
    }

    public void setOccurTime(LocalDateTime occurTime) {
        this.occurTime = occurTime;
    }

    public LocalDateTime getOccurTime() {
        return occurTime;
    }

    public long getHeapMemoryMax() {
        return heapMemoryMax;
    }

    public long getHeapMemoryUsed() {
        return heapMemoryUsed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorStack() {
        return errorStack;
    }

    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }

    public Integer getSendStatus() {
        return sendStatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppKey(){
        return appKey;
    }
}

