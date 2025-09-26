package com.it.weblogserver.ai.function;

import com.it.weblogclient.domain.MyLog;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class MyFunction {

    @Tool(description = "根据条件查询日志信息")
    public String myTest(@ToolParam(required = false,description = "日志查询条件") MyLog myLog){
        System.out.println("查询条件为" + myLog.getType() + myLog.getCreateTime());
        return "查询条件为" + myLog.getType() + myLog.getCreateTime();
    }
}
