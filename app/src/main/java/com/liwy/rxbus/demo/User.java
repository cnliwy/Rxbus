package com.liwy.rxbus.demo;

/**
 * Created by liwy on 2017/5/9.
 */

public class User {
    private String name;    //姓名
    private int age;        // 年龄
    private int sex;        //性别 1男 2女

    public User() {
    }

    public User(String name, int age, int sex) {
        this.name = name;
        this.age = age;
        this.sex = sex;
    }


    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", sex=" + sex +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }
}
