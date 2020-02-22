package com.changgou;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Date;

/**
 * @PackageName: com.changgou
 * @ClassName: BcriptTest
 * @Author: suibo
 * @Date: 2019/12/30 11:23
 * @Description: //Bcript对密码进行加密,BCript相对于MD5来说较为安全,因为有加盐的操作
 */
public class BcriptTest {
    public static void main(String[] args) {

        String hashpw = null;
        //每次生成的盐都不一样
        for (int i = 1; i <= 10; i++) {
            //获取29位随机的盐,前7位是版本和cost,用$作分隔符,后22位是盐
            String gensalt = BCrypt.gensalt();
            System.out.println(gensalt);
            //将盐与密码进行组合,形成密文
            hashpw = BCrypt.hashpw("111",gensalt);
            System.out.println(hashpw);
            //校验密码是否正确
            boolean checkpw = BCrypt.checkpw("111", hashpw);
            System.out.println(checkpw);

        }


    }
}
