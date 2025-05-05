package com.example.mobile_contact;

import android.graphics.Bitmap;

public class Contact {
    private String name;
    private String tel;
    private Bitmap img;

    public Contact(String name, String tel, Bitmap img) {
        this.name = name;
        this.tel = tel;
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }
}
