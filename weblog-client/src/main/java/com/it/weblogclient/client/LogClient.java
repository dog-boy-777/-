package com.it.weblogclient.client;


import com.alibaba.fastjson.JSONObject;
import com.it.weblogclient.dao.LocalLogStorage;
import com.it.weblogclient.domain.BasicMsg;
import com.it.weblogclient.domain.msg.LoginRequest;
import com.it.weblogclient.domain.msg.PendingBatch;
import com.it.weblogclient.domain.response.LoginResponse;
import com.it.weblogclient.domain.msg.WebLog;
import com.it.weblogclient.domain.msg.WebLogBatch;
import com.it.weblogclient.util.LocalFileLogger;
import com.it.weblogclient.util.SlidingWindow;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "web.log")
public class LogClient {

    private final NioEventLoopGroup woker = new NioEventLoopGroup();

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setName("LogClient-Reconnect-Thread");
        t.setDaemon(true);
        return t;
    });

    private final ScheduledExecutorService cleanExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setName("Clean-Thread");
        t.setDaemon(true);
        return t;
    });

    private Channel channel;

    private Bootstrap bootstrap;

    private boolean isShuttingDown = false;

    private volatile boolean isConnecting = false;

    // 在 LogClient 类中添加字段
    private volatile ScheduledFuture<?> logConsumerTask = null;

    // 当前重试次数，使用 volatile 保证可见性
    private volatile int retryCount = 1;

    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 3;

    private String appKey;

    private String gateWayUri;

    /**
     * 每个时间片的时长，以毫秒为单位
     */
    private int timeMillisPerSlice;
    /**
     * 共有多少个时间片（即窗口长度）
     */
    private int windowSize;
    /**
     * 在一个完整窗口期内允许通过的最大阈值
     */
    private int threshold;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getGateWayUri() {
        return gateWayUri;
    }

    public void setGateWayUri(String gateWayUri) {
        this.gateWayUri = gateWayUri;
    }

    public int getTimeMillisPerSlice() {
        return timeMillisPerSlice;
    }

    public void setTimeMillisPerSlice(int timeMillisPerSlice) {
        this.timeMillisPerSlice = timeMillisPerSlice;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public void start() {
        try {
            if (bootstrap == null) {
                bootstrap = new Bootstrap()
                        .group(woker)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();

                                // HTTP 编解码
                                pipeline.addLast(new HttpClientCodec());
                                // HTTP 消息聚合
                                pipeline.addLast(new HttpObjectAggregator(65536));
                                // 大数据流处理
                                pipeline.addLast(new ChunkedWriteHandler());

                                // 添加 WebSocket 客户端协议处理器
                                System.out.println("-------连接网址为：" + gateWayUri);
                                URI wsUri = new URI(gateWayUri); // 对应服务端 /im
                                HttpHeaders headers = new DefaultHttpHeaders();
                                WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory
                                        .newHandshaker(wsUri, WebSocketVersion.V13, null, false, headers);

                                pipeline.addLast(new WebSocketClientProtocolHandler(handshaker));

                                // 消息处理器
                                pipeline.addLast(new SimpleChannelInboundHandler<TextWebSocketFrame>() {

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame tx) throws Exception {
                                        String jsonStr = tx.text();
                                        BasicMsg basicMsg = JSONObject.parseObject(jsonStr, BasicMsg.class);

                                        // 登录应答
                                        if (basicMsg.getType().equals("login")) {
                                            LoginResponse response = JSONObject.parseObject(jsonStr, LoginResponse.class);
                                            System.out.println(jsonStr);
                                            // 连接成功
                                            if ("OK".equals(response.getIsConnected())) {
                                                System.out.println("连接建立成功");

                                                // 发送sqlLite里未发送的日志信息
                                                startLogConsumer();

                                                // 开启定期清理数据
                                                startCleanupTask();;
                                            } else {
                                                // 连接失败，关闭连接
                                                System.err.println("验证身份失败。gg");
                                                isShuttingDown = true;
                                                ctx.channel().close();
                                            }
                                        }

                                        // ack确认应答,从待确认队列里删除该日志
                                        else {
                                            System.out.println("接收到服务端ack------------");
                                            WebLogBatch webLogBatch = JSONObject.parseObject(jsonStr, WebLogBatch.class);
                                            String batchId = webLogBatch.getBatchId();

                                            // 批量更新sqlite数据库，移除待确认集合里的batchId
                                            if (ackMap.containsKey(batchId)) {
                                                LocalLogStorage.markAsSentBatch(ackMap.get(batchId).getIdList());
                                                ackMap.remove(batchId);
                                            }
                                        }
                                    }

                                    @Override
                                    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                        if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
                                            System.out.println("WebSocket 握手完成，发送登录请求...");
                                            // 传递appKey和滑动窗口参数
                                            LoginRequest loginRequest = new LoginRequest(appKey, timeMillisPerSlice, windowSize, threshold,"login");
                                            ctx.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(loginRequest)));
                                        } else {
                                            super.userEventTriggered(ctx, evt);
                                        }
                                    }
                                });
                            }
                        });
            }

            // 1. 解析 URI
            URI uri = new URI(gateWayUri);
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : (uri.getScheme().equals("wss") ? 443 : 80);

            ChannelFuture channelFuture = bootstrap.connect(host, port);
            System.out.println("连接日志服务器: " + uri.getScheme() + "://" + host + ":" + port + uri.getPath());

            channelFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    channel = future.channel();
                    System.out.println("连接到日志服务器成功");

                    // 重置重连次数
                    resetRetryCount();

                    // 等待channal关闭
                    channel.closeFuture().addListener((ChannelFutureListener) closeFuture -> {
                        System.err.println("channal关闭，执行重连...");
                        // 服务端炸了，开始重连
                        reConnection();
                    });
                } else {
                    System.err.println("连接日志服务器失败: " + future.cause().getMessage());
                    reConnection();
                }
            });

        } catch (Exception e) {
            System.err.println("异常" + e);
        }
    }

    /**
     * 重连方法
     */
    public synchronized void reConnection() {
        // 正常关闭则不用
        if (isShuttingDown)
            return;

        if (isConnecting)
            return;

        if (retryCount > MAX_RETRY_COUNT) {
            System.err.println("重连失败超过 " + MAX_RETRY_COUNT + " 次，不再尝试连接日志服务器。");
            return;
        }

        // 正在重连中
        isConnecting = true;

        // 发送sqlLite里未发送的日志信息
        startLogConsumer();

        // 开启定期清理数据
        startCleanupTask();;

        System.out.println("第" + retryCount + "次尝试重连日志服务器...");
        retryCount++;

        executorService.schedule(() -> {
            ChannelFuture channelFuture = bootstrap.connect("localhost", 8085);
            channelFuture.addListener((ChannelFutureListener) future -> {
                // 重置连接标识
                isConnecting = false;

                if (future.isSuccess()) {
                    channel = future.channel();
                    System.out.println("连接到日志服务器成功");

                    // 重置重连次数
                    resetRetryCount();

                    // 等待channal关闭
                    channel.closeFuture().addListener((ChannelFutureListener) closeFuture -> {
                        System.err.println("channal关闭，执行重连...");
                        // 服务端炸了，开始重连
                        reConnection();
                    });
                } else {
                    System.err.println("连接日志服务器失败: " + future.cause().getMessage());
                    reConnection();
                }
            });
        }, 10L, TimeUnit.SECONDS);
    }

    public void resetRetryCount() {
        retryCount = 1;
    }

    @PreDestroy
    public void stop() {
        if (channel != null && channel.isActive()) {
            try {
                isShuttingDown = true;
                channel.close().sync();
            } catch (Exception e) {
                System.err.println("连接中断" + e);
            } finally {
                woker.shutdownGracefully();
                executorService.shutdown();
            }
        }
    }


    public void sendErrorLog(Exception exception) {
        WebLog webLog = buildWebLog(exception);
        try {
            LocalLogStorage.saveLog(webLog); // 直接持久化，sent_status = 0
        } catch (Exception e) {
            System.err.println("日志持久化失败，异常原因：" + e);
        }
    }

    // 待确认集合:key为生成的UUID，value为消息的json字符串
    private final ConcurrentHashMap<String, PendingBatch> ackMap = new ConcurrentHashMap<>();

    private static final long ACK_TIMEOUT_MS = 3000; // 3秒超时
    private static final int MAX_RETRIES = 2;        // 最多重试2次


    private void startLogConsumer() {
        // 如果任务已存在且未被取消，则不再重复启动
        if (logConsumerTask != null && !logConsumerTask.isCancelled()) {
            return;
        }

        logConsumerTask = executorService.scheduleAtFixedRate(() -> {
            if (channel == null || !channel.isActive())
                return;

            // 检查是否有超时未确认的队列，重发
            handlerTimeOutMsg();

            // 发送新数据
            List<WebLog> webLogList = LocalLogStorage.getUnsentLogs(); // 查出未发送的50条数据
            if (webLogList.isEmpty()) {
                return;
            }
            String batchId = UUID.randomUUID().toString();
            WebLogBatch webLogBatch = new WebLogBatch(batchId, webLogList, "webLog");
            String jsonStr = JSONObject.toJSONString(webLogBatch);
            channel.writeAndFlush(new TextWebSocketFrame(jsonStr));

            // 添加到待确认队列
            List<Integer> idList = webLogList.stream().map(WebLog::getId).collect(Collectors.toList());
            PendingBatch pendingBatch = new PendingBatch(batchId, idList, jsonStr);
            ackMap.put(batchId, pendingBatch);
            LocalLogStorage.markAsSentWait(idList);

        }, 100, 500, TimeUnit.MILLISECONDS); // 每 500ms 消费一次
    }

    private void handlerTimeOutMsg() {
        long now = System.currentTimeMillis();
        for (PendingBatch pending : ackMap.values()) {
            if (now - pending.getSendTime() > ACK_TIMEOUT_MS) {
                if (pending.getRetryCount() < MAX_RETRIES) {
                    // 重试次数++
                    pending.incrementRetry();
                    String batchId = pending.getBatchId();
                    System.out.println("重发第 " + pending.getRetryCount() + " 次，BatchId: " + batchId);

                    channel.writeAndFlush(new TextWebSocketFrame(pending.getJsonStr()));

                    // 更新发送时间
                    PendingBatch pendingBatch = new PendingBatch(batchId, pending.getIdList(), pending.getJsonStr(), pending.getRetryCount());
                    ackMap.put(batchId, pendingBatch);

                } else {
                    System.err.println("批次 " + pending.getBatchId() + " 超过最大重试次数，标记为发送失败");
                    // 超过最大重试次数：写入本地文件 + 标记为失败
                    String batchId = pending.getBatchId();
                    System.err.println("批次 " + batchId + " 超过最大重试次数，写入本地文件并从待确认队列移除");

                    // 从待确认队列移除
                    ackMap.remove(batchId);

                    // 写到本地文件
                    LocalFileLogger.logFailedBatch(batchId, pending.getJsonStr());

                    // 可选：标记这些日志为“永久失败”
                    LocalLogStorage.markAsSentBatch(pending.getIdList());
                }
            }
        }
    }


    // 构建日志对象
    private WebLog buildWebLog(Exception exception) {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        long heapUsed = heapMemoryUsage.getUsed() / 1024 / 1024;
        long heapMax = heapMemoryUsage.getMax() / 1024 / 1024;

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);

        return new WebLog(heapUsed, heapMax, exception.getMessage(), sw.toString(), LocalDateTime.now(), "webLog");
    }


    // 在 LogClient 类中添加字段
    private volatile ScheduledFuture<?> cleanDataTask = null;

    // sqlLite数据最多存两天，到时间清空
    private void startCleanupTask() {
        // 如果任务已存在且未被取消，则不再重复启动
        if (cleanDataTask != null && !cleanDataTask.isCancelled()) {
            return;
        }
        cleanDataTask = cleanExecutor.scheduleWithFixedDelay(() -> {
            try {
                // 删除两天前的数据
                LocalLogStorage.deleteSentLogsOlderThanDays(2);
            } catch (Exception e) {
                System.err.println("清理旧日志失败: " + e.getMessage());
            }
        }, 3600, 1, TimeUnit.DAYS); // 每小时检查一次
    }
}
