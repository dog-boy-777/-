# -
自编写starter提供给别的项目作为netty客户端来收集异常信息，并传输给netty服务端展示给用户。

支持客户端重连，消息ACK确认，空闲心跳检测(这个忘记写了，大家自行补上就好，不难这个)和错误日志过多预警。

Netty使用了WebSocket协议，改为发送Http数据，可配合SpringCloudGateWay＋Nacos等服务调度中心拓展为微服务项目。（谁有好用的Maven镜像源甩两个给我呗，阿里云好多镜像都没有）

web-client就是编写的starter包，maven打包后就可以给其他项目使用了，里面封装了一个netty的客户端。

web-server是netty服务端，负责接收错误堆栈，支持SpringAi调用大模型实现Function Calling。

demo1是一个示例，引入了web-client打包而成的starter
