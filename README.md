# -
自编写starter提供给别的项目作为netty客户端来收集异常信息，并传输给netty服务端展示给用户。后续可拓展为微服务架构，使用Springboot Actuator暴露netty服务端运行情况，交由Nacos收集做健康检测，再使用SpringCloud GateWay根据客户端ip进行负载均衡。（个人感觉还是比较有价值的项目，点点start谢谢大家了！）
技术栈为：SpringBoot＋SpringAi+MongoTemplate+Netty+Redis+Sqlite

支持客户端重连，消息ACK确认，消息发送重试，空闲心跳检测(这个忘记写了，大家自行补上就好，不难这个)和错误日志过多预警。

Netty使用了WebSocket协议，改为发送Http数据，可配合SpringCloudGateWay＋Nacos等服务调度中心拓展为微服务项目。（谁有好用的Maven镜像源甩两个给我呗，阿里云好多镜像都没有）

web-client就是编写的starter包，maven打包后就可以给其他项目使用了，里面封装了一个netty的客户端。

web-server是netty服务端，负责接收错误堆栈，支持SpringAi调用大模型实现Function Calling。

demo1是一个示例，引入了web-client打包而成的starter


示例如下：

1.demo1启动：
<img width="1919" height="1087" alt="image" src="https://github.com/user-attachments/assets/3cf4f2c0-46b7-4ac4-ab8c-f7f11f4c0eda" />

1.服务端建立连接：
<img width="1919" height="1087" alt="image" src="https://github.com/user-attachments/assets/90385fb5-d0b2-4694-ad62-ab28d3bef7f2" />


2.demo1捕抓到异常信息，发送给服务端：
<img width="1919" height="1082" alt="image" src="https://github.com/user-attachments/assets/bd7099e3-4152-4978-9c99-10478d505df1" />

2.服务端返回批号的ack确认，并在redis存储批号，防止重复接收，将数据存到mongoDB：
<img width="1919" height="1078" alt="image" src="https://github.com/user-attachments/assets/ced366e4-711f-4994-9fda-e3ede870b58b" />


3.服务端突然挂了，客户端重连操作：
<img width="1919" height="1086" alt="image" src="https://github.com/user-attachments/assets/5459690c-2924-4c2b-8399-7c53d37d031a" />


4.客户端一直没收到服务端发来的ack确认，尝试重发，重发一定次数后不再发送，改为写到本地文件：
<img width="1919" height="1087" alt="image" src="https://github.com/user-attachments/assets/060c5201-f14d-4722-bd62-6420b69a8061" />








