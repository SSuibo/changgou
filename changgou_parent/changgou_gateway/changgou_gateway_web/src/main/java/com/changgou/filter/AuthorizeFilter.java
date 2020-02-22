package com.changgou.filter;

import com.changgou.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @PackageName: com.changgou.filter
 * @ClassName: AuthorizeFilter
 * @Author: suibo
 * @Date: 2020/1/13 17:25
 * @Description: //TODO
 */
@Component
public class AuthorizeFilter implements GlobalFilter,Ordered {

    public static final String Authorization = "Authorization";

    private static final String LOGIN_URL = "http://localhost:8001/api/oauth/toLogin";

    @Autowired
    private AuthService authService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();
        if("/api/oauth/login".equals(path) || !UrlFilter.hasAuthorize(path)){
            //判断是否为登录的操作,如果是登录操作,直接放行
            return chain.filter(exchange);
        }
        //不是登录,判断用户是否携带jti,如果携带,根据jti从redis查询jwt令牌,如果查询到就添加到headers里面进行访问其他资源
        String jti = authService.getJtiFromCookie(request);
        if(StringUtils.isEmpty(jti)){
            /*response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();*/
            return this.toLoginPage(LOGIN_URL+"?FROM="+request.getURI(),exchange);
        }
        //根据jti从redis中获取jwt
        String jwt = authService.getJwtFromRedis(jti);
        if(StringUtils.isEmpty(jwt)){
            /*response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();*/
            return this.toLoginPage(LOGIN_URL+"?FROM="+request.getURI(),exchange);
        }
        //jti和jwt都有了,就要对请求进行增强,将jwt令牌携带到header里面
        request.mutate().header(Authorization,"Bearer " + jwt);

        return chain.filter(exchange);
    }

    //跳转登录页面
    private Mono<Void> toLoginPage(String loginUrl,ServerWebExchange exchange){
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().set("location",loginUrl);
        return response.setComplete();
    }

    //值越小,越先执行
    @Override
    public int getOrder() {
        return 0;
    }
}
