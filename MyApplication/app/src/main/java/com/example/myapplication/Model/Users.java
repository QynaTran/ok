package com.example.myapplication.Model;

public class Users {
    public String name;
    public String image;
    public String status;
    public String thump_image;

    public Users() {
    }

    public Users(String name, String image, String status, String thump_image) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thump_image = thump_image;

    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThump_image() {
        return thump_image;
    }

    public void setThump_image(String thump_image) {
        this.thump_image = thump_image;
    }
}
