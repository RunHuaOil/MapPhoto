package com.runhuaoil.mapphotosrunhua.bean;

import android.graphics.drawable.Drawable;

/**
 * Created by Administrator on 2016/1/2.
 */
public  class RowInfoBean {

    public int  id; // 相册 id
    public Drawable  thumb; // 相册图标
    public String  title,addr; // 相册标题


    public RowInfoBean(Drawable thumb, String title) {
        this.thumb = thumb;
        this.title = title;
    }

    public RowInfoBean() {

    }
}