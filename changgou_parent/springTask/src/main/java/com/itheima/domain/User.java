package com.itheima.domain;

/**
 * @PackageName: com.itheima.domain
 * @ClassName: User
 * @Author: suibo
 * @Date: 2020/2/3 15:06
 * @Description: //TODO
 */
public class User {
    private String username;
    private Integer age;

    public User() {
    }

    public User(String username, Integer age) {
        this.username = username;
        this.age = age;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
