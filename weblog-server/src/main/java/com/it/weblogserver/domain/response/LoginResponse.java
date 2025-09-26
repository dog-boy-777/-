package com.it.weblogserver.domain.response;

import com.it.weblogserver.domain.BasicMsg;

public class LoginResponse extends BasicMsg {
    private String isConnected;

    public LoginResponse(String isConnected, String type){
        this.isConnected = isConnected;
        this.type = type;
    }

    public String getIsConnected() {
        return isConnected;
    }

    public void setIsConnected(String isConnected) {
        this.isConnected = isConnected;
    }
}
