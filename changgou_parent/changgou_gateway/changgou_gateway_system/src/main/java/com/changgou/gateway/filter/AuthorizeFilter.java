package com.changgou.gateway.filter;

import com.changgou.gateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @PackageName: com.changgou.gateway.listener
 * @ClassName: AuthorizeFilter
 * @Author: suibo
 * @Date: 2020/1/2 18:09
 * @Description: //网关微服务检验token
 */
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("网关微服务检验token的过滤器");
        //1.获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        //2.获取响应对象,为了给页面响应错误信息
        ServerHttpResponse response = exchange.getResponse();
        //3.获取用户输入的url,目的是为了判断输入的url中是否包含登录的url
        if(request.getURI().getPath().contains("/admin/login")){
            //用户确实是在登录操作
            return chain.filter(exchange);
        }
        //4.获取请求头
        HttpHeaders headers = request.getHeaders();
        //5.从请求头中获取令牌
        String token = headers.getFirst("token");
        //6.判断请求头中是否有令牌
        if(StringUtils.isEmpty(token)){
            //是空的,没有携带令牌,就返回一个没有权限的信息
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //不携带任何数据返回
            return response.setComplete();
        }
        //7.解析令牌
        try {
            JwtUtil.parseJWT(token);
        } catch (Exception e) {
            e.printStackTrace();
            //进入catch说明解析失败,token错误或者过期
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //如果以上条件都满足,就放行
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
