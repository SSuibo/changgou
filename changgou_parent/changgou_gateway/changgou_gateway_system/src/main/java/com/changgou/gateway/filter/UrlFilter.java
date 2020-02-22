package com.changgou.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @PackageName: com.changgou.gateway.listener
 * @ClassName: UrlFilter
 * @Author: suibo
 * @Date: 2019/12/30 10:13
 * @Description: //自定义全局过滤器,获取客户端的url
 */
@Component
public class UrlFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("经过第二个过滤器URLFilter");
        ServerHttpRequest request = exchange.getRequest();
        String url = request.getURI().getPath();
        System.out.println("url: " + url);
        return chain.filter(exchange);
    }

    //值越小越先执行,IpFilter的值为1,所以IpFilter先执行
    @Override
    public int getOrder() {
        return 3;
    }
}
