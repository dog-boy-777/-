package com.it.weblogserver.netty;

import com.it.weblogserver.netty.handler.WebSocketFrameHandler;
import com.it.weblogserver.utils.SlidingWindow;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class MyServerChannal {

    public static final ConcurrentHashMap<String, Channel> userChannal = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String, SlidingWindow> windows = new ConcurrentHashMap<>();

    @Autowired
    private WebSocketFrameHandler webSocketFrameHandler;

    @PostConstruct
    public void start() {
        Thread thread = new Thread(() -> {
            NioEventLoopGroup boss = new NioEventLoopGroup();
            NioEventLoopGroup worker = new NioEventLoopGroup();

            try {
                ChannelFuture channelFuture = new ServerBootstrap()
                        .group(boss, worker)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                // 添加 Http 编码解码器
                                ch.pipeline().addLast(new HttpServerCodec())
                                        // 添加处理大数据的组件
                                        .addLast(new ChunkedWriteHandler())
                                        // 对 Http 消息做聚合操作方便处理，产生 FullHttpRequest 和 FullHttpResponse
                                        // 1024 * 64 是单条信息最长字节数
                                        .addLast(new HttpObjectAggregator(1024 * 64))
                                        // 添加 WebSocket 支持
                                        .addLast(new WebSocketServerProtocolHandler("/im"));

                                // 登录认证handler
                                ch.pipeline().addLast(webSocketFrameHandler);

                            }
                        }).bind(8085).sync();

                System.out.println("netty服务端启动完成");
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                System.out.println("连接断开了" + e);
            } finally {
                boss.shutdownGracefully();
                worker.shutdownGracefully();
            }
        });

        thread.setDaemon(true);
        thread.start();
    }
}
