package com.it.weblogserver.netty.handlerFactory;

import com.it.weblogserver.annoation.NettyHandlerType;
import com.it.weblogserver.netty.strateg.BasicStrateg;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HandlerFactory {

    private final ConcurrentHashMap<String, BasicStrateg> handlerMap = new ConcurrentHashMap<>();

    public HandlerFactory(Map<String, BasicStrateg> handlerBeans){
        handlerBeans.values().forEach(handler -> {
            Class<? extends BasicStrateg> clazz = handler.getClass();
            if(clazz.isAnnotationPresent(NettyHandlerType.class)){
                String type = clazz.getAnnotation(NettyHandlerType.class).value();
                handlerMap.put(type,handler);
            }
        });
    }

    public BasicStrateg getHandler(String type){
        if (!handlerMap.containsKey(type)) {
            throw new IllegalArgumentException("不支持的消息类型: " + type);
        }
        return handlerMap.get(type);
    }
}
