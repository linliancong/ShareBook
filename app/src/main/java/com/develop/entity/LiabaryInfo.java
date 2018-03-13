package com.develop.entity;

/**
 * Created by Administrator on 2018/3/13.
 */

public class LiabaryInfo {
    private String name;
    private String count;

    public LiabaryInfo(String name,String count){
        this.name =name;
        this.count =count;
    }

    public String getName() {
        return name;
    }

    public String getCount() {
        return count;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
