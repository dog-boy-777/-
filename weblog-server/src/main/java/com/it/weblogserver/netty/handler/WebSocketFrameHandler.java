package com.it.weblogserver.netty.handler;

import com.alibaba.fastjson.JSONObject;
import com.it.weblogserver.domain.BasicMsg;
import com.it.weblogserver.netty.MyServerChannal;
import com.it.weblogserver.netty.handlerFactory.HandlerFactory;
import com.it.weblogserver.netty.strateg.BasicStrateg;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ChannelHandler.Sharable
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Autowired
    HandlerFactory handlerFactory;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame tx) throws Exception {
        BasicMsg basicMsg = JSONObject.parseObject(tx.text(), BasicMsg.class);

        BasicStrateg handler = handlerFactory.getHandler(basicMsg.getType());

        handler.handler(ctx,tx);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String appKey = (String) ctx.channel().attr(AttributeKey.valueOf("appKey")).get();
        if (appKey != null) {
            MyServerChannal.userChannal.remove(appKey);
            MyServerChannal.windows.remove(ctx.channel());
            log.info("连接已关闭，清理资源: appKey={}", appKey);
        }
        super.channelInactive(ctx);
    }
}
