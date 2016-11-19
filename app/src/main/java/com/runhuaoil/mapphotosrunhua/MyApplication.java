package com.runhuaoil.mapphotosrunhua;

import android.app.Application;
import android.content.Context;
import com.baidu.mapapi.SDKInitializer;


/**
 * Created by Administrator on 2015/12/31.
 */
public class MyApplication extends Application{

    private static Context context;

    private  MyUserDataHelper userdatabase;


    @Override
    public void onCreate() {
        context = getApplicationContext();
        SDKInitializer.initialize(getApplicationContext());
        userdatabase = new MyUserDataHelper(getApplicationContext(),"UserDataBase",null,1);
        userdatabase.getWritableDatabase();

        super.onCreate();
    }

    public static Context getContext(){
        return context;
    }




}
