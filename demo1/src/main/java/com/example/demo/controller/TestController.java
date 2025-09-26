package com.example.demo.controller;

import com.example.demo.domain.TestJson;
import com.example.demo.mapper.TestMapper;
import com.it.weblogclient.client.LogClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    TestMapper testMapper;

    @Autowired
    LogClient logClient;

    @RequestMapping("/test")
    public void test() {
        System.err.println("开始报错");
        int i = 1 / 0;
    }

    @RequestMapping("/sayHello")
    public void hello() {
        try {
            testMapper.inertTest(new TestJson("sddada"));
        } catch (Exception e) {
            System.out.println(e.getClass());
            logClient.sendErrorLog(e);
        }
    }
}
