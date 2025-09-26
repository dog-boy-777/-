//package com.it.weblogclient.fliter;
//
//import cn.hutool.json.JSONUtil;
//import com.alibaba.fastjson.JSON;
//import com.it.weblogclient.domain.LoginUser;
//import com.it.weblogclient.properties.JwtProperties;
//import com.it.weblogclient.utils.JwtUtil;
//import io.jsonwebtoken.Claims;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.Objects;
//
//// OncePerRequestFilter特点是在处理单个HTTP请求时确保过滤器的 doFilterInternal 方法只被调用一次
//@Component
//@Slf4j
//public class JwtTokenFliter extends OncePerRequestFilter {
//
//    @Autowired
//    JwtProperties jwtProperties;
//
//    @Autowired
//    RedisTemplate redisTemplate;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        log.info("经过JwtTokenFliter, 当前访问路径：{}", request.getRequestURI());
//
//        // 1.从请求中获取令牌
//        String jwt = request.getHeader(jwtProperties.getTokenName());
//
//        // 1.1判断是否为空或者长度为0
//        if(!StringUtils.hasLength(jwt)){
//            //没有token放行 此时的SecurityContextHolder没有用户信息 会被后面的过滤器拦截
//            System.out.println("-----放行");
//            filterChain.doFilter(request,response);
//            return;
//        }
//
//        // 2.校验令牌
//        String userId;
//        try {
//            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), jwt);
//            userId = claims.getSubject();
//        } catch (Exception e) {
//            // 3.解析jwt失败，响应401
//            throw new RuntimeException("token非法");
//        }
//
//        // 4.查询redis判断下用户登录状态
//        String jsonString = JSON.toJSONString(redisTemplate.opsForValue().get("login_" + userId));
//        LoginUser loginUser = JSONUtil.toBean(jsonString, LoginUser.class);
//        if(Objects.isNull(loginUser))
//            throw new RuntimeException("当前未登录");
//
//        // 5.通过，往SecurityContextHolder里存储用户信息进去认证，放行
//        log.info("token认证成功");
//        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(loginUser,null,null));
//        filterChain.doFilter(request,response);
//
//        // 6.在请求返回后清空SecurityContextHolder，防止内存泄露
//        System.out.println("清空SecurityContextHolder");
//        SecurityContextHolder.clearContext();
//    }
//}
