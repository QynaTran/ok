package com.example.myapplication.Model;

public class Friends extends Users {
    public String date;

    public Friends() {
    }

    public Friends(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
