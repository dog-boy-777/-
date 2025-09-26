package com.example.demo.handler;

import com.it.weblogclient.client.LogClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Component
public class GlobalExceptionHandler {

    @Autowired
    LogClient logClient;


    @ExceptionHandler(value = Exception.class)
    public void handleException(Exception e){
        System.err.println("全局异常处理器捕抓到异常:" + e);
        System.out.println(e.getClass());
        logClient.sendErrorLog(e);
    }

}
