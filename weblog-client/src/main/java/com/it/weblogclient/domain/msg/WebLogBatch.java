package com.it.weblogclient.domain.msg;

import com.it.weblogclient.domain.BasicMsg;

import java.util.List;

public class WebLogBatch extends BasicMsg {

    private String batchId;

    private List<WebLog> webLogList;

    public WebLogBatch(){}

    public WebLogBatch(String batchId, List<WebLog> webLogList, String type){
        this.batchId = batchId;
        this.webLogList = webLogList;
        this.type = type;
    }

    public String getBatchId(){
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public List<WebLog> getWebLogList() {
        return webLogList;
    }

    public void setWebLogList(List<WebLog> webLogList) {
        this.webLogList = webLogList;
    }
}
