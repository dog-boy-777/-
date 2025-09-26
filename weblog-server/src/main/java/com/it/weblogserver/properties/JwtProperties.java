package com.it.weblogserver.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "web.jwt")
@Component
@Data
public class JwtProperties {
    // jwt密钥
    String secretKey;
    // 过期时间
    long ttl;
    // 前端传的令牌名字
    String tokenName;
}
