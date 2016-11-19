package com.runhuaoil.mapphotosrunhua;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Administrator on 2015/12/31.
 */
public class RegisterActivity extends BaseActivity implements View.OnClickListener{

    private EditText nameEdit1,pswEdit1;
    private Button buttonReg;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.register_layout);

            MyUserDataHelper dbHelper = new MyUserDataHelper(this,"UserDataBase",null,1);
            db = dbHelper.getWritableDatabase();

            buttonReg = (Button) findViewById(R.id.register_button);
            nameEdit1 = (EditText) findViewById(R.id.accountReg);
            pswEdit1 = (EditText) findViewById(R.id.passwordReg);

            buttonReg.setOnClickListener(this);

        }


    @Override
    public void onClick(View v) {


        if (nameEdit1.getText().toString().equals("") || pswEdit1.getText().toString().equals("")){

            Toast.makeText(this,"账号和密码不能为空",Toast.LENGTH_SHORT).show();

            return;
        }


        if (checkName()) {
            ContentValues values = new ContentValues();
            switch (v.getId()){
                case R.id.register_button:

                    values.put("username", nameEdit1.getText().toString());
                    values.put("password", pswEdit1.getText().toString());
                    values.put("userDataBaseName", "DB" + nameEdit1.getText().toString() + "PhotoDataBase");

                    db.insert("user", null, values);
                    values.clear();
                    Toast.makeText(this,"注册成功",Toast.LENGTH_LONG).show();

                    Intent intentRe = new Intent();
                    intentRe.putExtra("username",nameEdit1.getText().toString());

                    setResult(RESULT_OK,intentRe);

                    finish();

                    break;
                default:
                    break;
            }

        }else{
            Toast.makeText(this,"已存在账号名,请重新注册",Toast.LENGTH_SHORT).show();
            nameEdit1.setText("");
            pswEdit1.setText("");
            nameEdit1.setFocusable(true);
            nameEdit1.setFocusableInTouchMode(true);
            nameEdit1.requestFocus();
        }



    }

    private boolean checkName() {

        //int username1 = Integer.parseInt(nameEdit1.getText().toString());

        Cursor cur = db.query("user", new String[]{"username"} ,"username = ?" ,new String[]{nameEdit1.getText().toString()}, null, null, null);

        if (cur.moveToFirst()){
            cur.close();
            return false;
        }else{
            cur.close();
            return true;
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("username", nameEdit1.getText().toString());
        setResult(RESULT_OK, intent);
        finish();

    }
}
