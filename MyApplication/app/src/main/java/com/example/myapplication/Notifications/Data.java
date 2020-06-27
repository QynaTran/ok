package com.example.myapplication.Notifications;

public class Data {
    private String user, title, body, sent, type;
    private Integer icon;

    public Data() {
    }

    public Data(String user, String title, String body, String sent, String type, Integer icon) {
        this.user = user;
        this.title = title;
        this.body = body;
        this.sent = sent;
        this.type = type;
        this.icon = icon;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }
}