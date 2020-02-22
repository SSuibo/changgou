package com.changgou.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * @PackageName: com.changgou.gateway.listener
 * @ClassName: IpFilter
 * @Author: suibo
 * @Date: 2019/12/30 9:53
 * @Description: //自定义全局过滤器,获取客户端的ip
 */
@Component      //将这个bean注入spring容器,交由spring来管理
public class IpFilter implements GlobalFilter,Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("经过第一个过滤器IpFilter");
        ServerHttpRequest request = exchange.getRequest();
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        System.out.println("ip: " + remoteAddress.getHostName());
        return chain.filter(exchange);
    }

    //值越小越先执行
    @Override
    public int getOrder() {
        return 1;
    }
}
