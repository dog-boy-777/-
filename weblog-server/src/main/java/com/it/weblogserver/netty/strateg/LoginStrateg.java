package com.it.weblogserver.netty.strateg;

import com.alibaba.fastjson.JSONObject;
import com.it.weblogserver.annoation.NettyHandlerType;
import com.it.weblogserver.domain.msg.LoginRequest;
import com.it.weblogserver.domain.response.LoginResponse;
import com.it.weblogserver.netty.MyServerChannal;
import com.it.weblogserver.utils.SlidingWindow;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@NettyHandlerType("login")
public class LoginStrateg implements BasicStrateg {

    @Override
    public void handler(ChannelHandlerContext ctx, TextWebSocketFrame tx) {
        LoginRequest loginRequest = JSONObject.parseObject(tx.text(), LoginRequest.class);

        LoginResponse loginResponse;
        String appKey = loginRequest.getAppKey();
        int timeMillisPerSlice = loginRequest.getTimeMillisPerSlice();
        int windowSize = loginRequest.getWindowSize();
        int threshold = loginRequest.getThreshold();

        if("tell me the true".equals(appKey)){
            log.info("有连接建立",ctx.channel());
            MyServerChannal.userChannal.put(appKey, ctx.channel());

            // 把appkey绑定到channal上
            ctx.channel().attr(AttributeKey.valueOf("appKey")).set(appKey);

            // 根据参数构建滑动窗口
            MyServerChannal.windows.put(appKey, new SlidingWindow(timeMillisPerSlice, windowSize, threshold));

            // 返回应答
            loginResponse = new LoginResponse("OK","login");
        }
        else
            // 返回应答
            loginResponse = new LoginResponse("ERROR","login");

        ctx.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(loginResponse)));
    }
}
