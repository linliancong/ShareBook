package com.develop.entity;

import com.develop.sharebook.Message;

/**
 * Created by Administrator on 2018/3/15.
 */

public class Messages {
    private String title;
    private String content;
    private String date;
    private int id;

    public Messages(){}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
