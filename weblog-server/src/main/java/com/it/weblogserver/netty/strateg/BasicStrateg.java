package com.it.weblogserver.netty.strateg;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public interface BasicStrateg {

    public void handler(ChannelHandlerContext ctx, TextWebSocketFrame tx);
}
