package com.changgou.business.listener;

import okhttp3.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @PackageName: com.changgou.business.listener
 * @ClassName: AdFilter
 * @Author: suibo
 * @Date: 2020/1/4 15:42
 * @Description: //监听rabbit中的消息,一旦监听到消息就消费掉
 */
@Component
public class AdListener {

    @RabbitListener(queues = "ad_update_queue")
    public void recieveMessage(String message){
        System.out.println("监听到的消息是: " + message);
        //需要用okhttp远程调用nginx
        OkHttpClient okHttpClient = new OkHttpClient();
        String url = "http://192.168.200.128/ad_update?position=" + message;
        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //请求失败
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //请求成功
                System.out.println("请求成功:" + response.message());
            }
        });
    }

}
