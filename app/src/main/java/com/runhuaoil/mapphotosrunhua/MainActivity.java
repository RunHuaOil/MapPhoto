package com.runhuaoil.mapphotosrunhua;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends BaseActivity implements View.OnClickListener{


    private SharedPreferences.Editor editor;
    private SharedPreferences pref;
    private Button registerButton,loginButton;
    private EditText nameEdit,pswEdit;
    private MyUserDataHelper dbHelper;
    private CheckBox rememberPass;
    private static final int REG_RESULT = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new MyUserDataHelper(this,"UserDataBase",null,1);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        rememberPass = (CheckBox) findViewById(R.id.remember_pass);
        registerButton = (Button) findViewById(R.id.register);
        loginButton = (Button) findViewById(R.id.login);
        nameEdit = (EditText) findViewById(R.id.account);
        pswEdit = (EditText) findViewById(R.id.password);

        registerButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);

        boolean isRemember = pref.getBoolean("remember_password", false);
        if (isRemember) {


            nameEdit.setText(pref.getString("account", ""));
            pswEdit.setText(pref.getString("password", ""));
            rememberPass.setChecked(true);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (item.getItemId() == R.id.item_exit) {
            ActivityCollector.finishAll();
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //ContentValues values = new ContentValues();
        switch (v.getId()){
            case R.id.register:

                Intent intentReg = new Intent(this,RegisterActivity.class);
                startActivityForResult(intentReg,REG_RESULT);
                break;
            case R.id.login:

                if (nameEdit.getText().toString().equals("") || pswEdit.getText().toString().equals("")){

                    Toast.makeText(this,"账号和密码不能为空",Toast.LENGTH_SHORT).show();

                    return;
                }

                Cursor cur =  db.query("user", new String[]{"*"}, "username = ? and password= ?",
                        new String[]{nameEdit.getText().toString(), pswEdit.getText().toString()}, null, null, null);

                if (cur.moveToFirst()) {
                    Toast.makeText(this,"验证成功！",Toast.LENGTH_LONG).show();
                    String name,psw,db1;
                    do {
                         name = cur.getString(cur.getColumnIndex("username"));
                         psw = cur.getString(cur.getColumnIndex("password"));
                         db1 = cur.getString(cur.getColumnIndex("userDataBaseName"));
                    } while (cur.moveToNext());

                    editor = pref.edit();
                    if (rememberPass.isChecked()) {
                        editor.putBoolean("remember_password", true);
                        editor.putString("account", name);
                        editor.putString("password", psw);
                    } else {
                        editor.clear();
                    }
                    editor.commit();

                    Intent intentMapGE = new Intent(this,MapPhotoGE.class);
                    intentMapGE.putExtra("databaseName",db1);
                    startActivity(intentMapGE);

                    finish();
                }else{
                    Toast.makeText(this,"用户名或者密码错误，请重新登陆",Toast.LENGTH_SHORT).show();

                    pswEdit.setText("");
                }
                cur.close();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REG_RESULT:
                if (resultCode == RESULT_OK) {
                    String returnedData = data.getStringExtra("username");
                    nameEdit.setText(returnedData);
                    pswEdit.setText("");
                    pswEdit.setFocusable(true);
                    pswEdit.setFocusableInTouchMode(true);
                    pswEdit.requestFocus();
                }
                break;
            default:
                break;
        }
    }
}
