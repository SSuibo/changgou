package com.changgou.filter;

/**
 * @PackageName: com.changgou.oauth.filter
 * @ClassName: UrlFilter
 * @Author: suibo
 * @Date: 2020/1/13 19:11
 * @Description: //路径过滤
 */
public class UrlFilter {
    public static String filterPath = "/api/wseckillorder,/api/seckill,/api/wxpay," +
            "/api/wxpay/**,/api/worder/**,/api/user/**,/api/address/**,/api/wcart/**," +
            "/api/cart/**,/api/categoryReport/**,/api/ord erConfig/**,/api/order/**," +
            "/api/orderItem/**,/api/orderLog/**,/api/preferential/* *,/api/returnCause/**," +
            "/api/returnOrder/**,/api/returnOrderItem/**";

    public static boolean hasAuthorize(String url){
        String[] split = filterPath.replace("**", "").split(",");
        for (String value : split) {
            if(url.startsWith(value)){
                return true;    //代表当前的访问地址是需要传递令牌的
            }
        }
        return false;
    }
}
