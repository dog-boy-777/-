package com.it.weblogserver.netty.strateg;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.it.weblogserver.annoation.NettyHandlerType;
import com.it.weblogserver.domain.msg.WebLog;
import com.it.weblogserver.domain.msg.WebLogBatch;
import com.it.weblogserver.netty.MyServerChannal;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@NettyHandlerType("webLog")
public class WebLogStrateg implements BasicStrateg {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public void handler(ChannelHandlerContext ctx, TextWebSocketFrame tx) {
        System.err.println("接收到的错误日志" + tx.text());

        WebLogBatch webLogBatch = JSONObject.parseObject(tx.text(), WebLogBatch.class);
        String batchId = webLogBatch.getBatchId();

        // 防止重复，判断redis里是否存在这条消息，过期时间设为15秒
        Boolean isExit = redisTemplate.hasKey(batchId);

        // 之前未收到过该消息
        if(!isExit){
            redisTemplate.opsForValue().set(batchId,null,15L, TimeUnit.SECONDS);
            List<WebLog> webLogList = webLogBatch.getWebLogList();
            String appKey = (String) ctx.channel().attr(AttributeKey.valueOf("appKey")).get();
            boolean isDanger = MyServerChannal.windows.get(appKey).addCount(webLogList.size());

            // todo,投到消息队列里进行异步操作
            if(isDanger)
                System.err.println("----------完蛋了，报错太多了");
            webLogList.forEach(webLog -> webLog.setAppKey(appKey));
            mongoTemplate.insertAll(webLogList);
        }

        // 无论之前是否收到都要向客户端发送ack确认应答
        webLogBatch.setWebLogList(null);
        String jsonStr = JSON.toJSONString(webLogBatch);
        System.out.println("发送ack确认--------" + jsonStr);
        ctx.writeAndFlush(new TextWebSocketFrame(jsonStr));
    }
}
