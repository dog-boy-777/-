package com.it.weblogclient.domain.response;

import com.it.weblogclient.domain.BasicMsg;

public class LoginResponse extends BasicMsg {
    private String isConnected;

    public LoginResponse(String isConnected){
        this.isConnected = isConnected;
    }

    public String getIsConnected() {
        return isConnected;
    }

    public void setIsConnected(String isConnected) {
        this.isConnected = isConnected;
    }
}
