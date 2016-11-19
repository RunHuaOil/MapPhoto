package com.runhuaoil.mapphotosrunhua;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Administrator on 2015/12/31.
 */
public class MyUserDataHelper extends SQLiteOpenHelper {


    public static final String CREATE_USERDATA =  "create table user ("
            + "id integer primary key autoincrement,"
            + "username varchar(20) not null,"
            + "userDataBaseName varchar(20) not null,"
            + "password varchar(20) not null)";


    //private Context mcontext;

    public MyUserDataHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {

        super(context, name, factory, version);
        //mcontext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("RunHua","创建用户表成功");
        db.execSQL(CREATE_USERDATA);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
