package com.changgou.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

/**
 * @PackageName: com.changgou.service
 * @ClassName: AuthService
 * @Author: suibo
 * @Date: 2020/1/13 17:52
 * @Description: //TODO
 */
@Service
public class AuthService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //从cookie获得jti的方法
    public String getJtiFromCookie(ServerHttpRequest request) {
        HttpCookie httpCookie = request.getCookies().getFirst("uid");
        if(httpCookie!=null){
            String jti = httpCookie.getValue();
            return jti;
        }
        return null;
    }

    //从redis根据jti获取jwt的方法
    public String getJwtFromRedis(String jti) {
        return stringRedisTemplate.boundValueOps(jti).get();
    }
}
