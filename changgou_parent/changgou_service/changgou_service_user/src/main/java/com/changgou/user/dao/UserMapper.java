package com.changgou.user.dao;

import com.changgou.user.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface UserMapper extends Mapper<User> {

    //用户增加积分
    @Update("update tb_user set points=points+#{points} where username=#{username}")
    int addPoints(@Param("username") String username, @Param("points") Integer points);

    @Update("update tb_user set points = points+#{point} where username = #{username}")
    int updateUserPoint(@Param("username") String username, @Param("point") int point);
}
