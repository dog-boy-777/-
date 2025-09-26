package com.it.weblogclient.config;
import com.it.weblogclient.client.LogClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "web.log",value = "open")
@EnableConfigurationProperties(LogClient.class)
public class WeblogAutoConfig {

    // 手动注入，代替@Compoenent注解的功能
//    @Bean
//    @ConditionalOnMissingBean
//    public WebLogProperties webLogProperties(){
//        System.out.println("配置类已注入");
//        return new WebLogProperties();
//    }

    // 手动注入，代替@Compoenent注解的功能
    @Bean
    @ConditionalOnMissingBean
    public LogClient logClient(){
        System.out.println("netty客户端已注入");
        LogClient logClient = new LogClient();

        // 启动netty客户端
        Thread thread = new Thread(() -> {
            logClient.start();
        });
        thread.setDaemon(true);
        thread.start();

        return logClient;
    }
}
