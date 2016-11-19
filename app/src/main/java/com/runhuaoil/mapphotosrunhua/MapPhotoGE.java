package com.runhuaoil.mapphotosrunhua;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.runhuaoil.mapphotosrunhua.bean.RowInfoBean;
import com.runhuaoil.mapphotosrunhua.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class MapPhotoGE extends BaseActivity {



    public static final int REQUEST_MAPVIEW = 11;

    private ListView photoListView;

    private int seledRowIndex = -1;
    private  MyUserDataHelper userdatabase;

    private List<RowInfoBean> photoList = new ArrayList<RowInfoBean>();
    private String userPhotoTable;
    private PhotoAdapter  photoAdapter;
    private MenuItem  editMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_photo_ge);

        userPhotoTable = getIntent().getStringExtra("databaseName");

        userdatabase = new MyUserDataHelper(getApplicationContext(),"UserDataBase",null,1);

        initDB();


        photoListView = (ListView) findViewById(R.id.photoListView );
        photoAdapter = new PhotoAdapter(this);
        photoListView.setAdapter(photoAdapter);



        photoListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {


            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (seledRowIndex == position) {
                    seledRowIndex = -1;
                    editMenu.setEnabled(false);
                } else {
                    seledRowIndex = position;
                    editMenu.setEnabled(true);
                }

                photoAdapter.notifyDataSetInvalidated();
                return  true;
            }
        });
    }

    private void initDB() {


        photoList.clear();
        SQLiteDatabase db = userdatabase.getWritableDatabase();
        Drawable defaultThumb = getResources().getDrawable(R.drawable.emblem);

        String CREATE_PHOTOSDATA =  "create table if not exists "+ userPhotoTable + " ( "
                + " id integer primary key autoincrement, "
                + " latitude double, "
                + " longitude double, "
                + " address text, "
                + " thumb varchar, "
                + " title varchar, "
                + " picture varchar ) ";

        db.execSQL(CREATE_PHOTOSDATA);


        Cursor cursor = db.query(userPhotoTable,null,null,null,null,null,null);

        while(cursor.moveToNext()) {
            RowInfoBean bean = new RowInfoBean();

            bean.id = cursor.getInt(cursor.getColumnIndex("id"));
            bean.title = cursor.getString(cursor.getColumnIndex("title"));
            bean.addr = cursor.getString(cursor.getColumnIndex("address"));
            String thumb = cursor.getString(cursor.getColumnIndex("thumb"));

            if (thumb == null || thumb.equals("")) {
                bean.thumb = defaultThumb;
            } else {
                thumb = CommonUtils.THUMB_PATH + thumb;
                bean.thumb = new BitmapDrawable(getResources(), BitmapFactory.decodeFile(thumb));
            }
            photoList.add(bean);
        }
        cursor.close();
        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_map_photo_ge, menu);
        // 禁用"修改名称"菜单项
        editMenu = menu.findItem(R.id.menu_item_edit );
        editMenu.setEnabled(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id. menu_item_gallery :
                Intent intentGallery = new Intent( this, GalleryActivity.class);
                intentGallery.putExtra( "userDataTable", userPhotoTable);
                startActivity(intentGallery);
                return true;

            case R.id. menu_item_add :
                final EditText txtTitle = new EditText(this);
                txtTitle.setInputType(InputType.TYPE_CLASS_TEXT );
                // 动态创建对话框
                AlertDialog.Builder dialog = new AlertDialog.Builder( this);
                // 设定对话框中的按钮（修改和返回）
                dialog.setPositiveButton("确定",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,int which){
                            String title = txtTitle.getText().toString();
                        // 将新增条目数据保存到数据库
                            SQLiteDatabase db = userdatabase.getWritableDatabase();
                            ContentValues values = new ContentValues();

                            values.put("title", title);
                            db.insert(userPhotoTable, null, values);
                            db.close();
                        // 重新加载数据库数据并显示
                            initDB();
                            photoAdapter.notifyDataSetChanged();

                            seledRowIndex = -1;
                            editMenu.setEnabled(false);

                        }
                 });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                // 设定对话框标题和输入框，然后显示
                dialog.setTitle("新相册名称");
                dialog.setView(txtTitle);
                dialog.show();

                return true;
            case R.id. menu_item_remove :
                if (seledRowIndex != -1) {
                    RowInfoBean bean = photoList.get(seledRowIndex);
                    SQLiteDatabase db = userdatabase.getWritableDatabase();

                    String id = bean.id+"";
                    db.delete(userPhotoTable, "id = ?", new String[]{id});
                    db.close();

                    initDB();
                    photoAdapter.notifyDataSetChanged();

                    seledRowIndex = -1;
                    editMenu.setEnabled(false);



                } else {
                    Toast. makeText (getApplicationContext(),"长按数据行以选中,再执行删除操作",Toast. LENGTH_SHORT ).show();
                }
                return true;

            case R.id.menu_item_edit :

                final RowInfoBean bean =  photoList.get(seledRowIndex);
                final String beanID = bean.id+"";

                final EditText input = new EditText( this);
                input.setInputType(InputType.TYPE_CLASS_TEXT );
                input.setText(bean.title);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setPositiveButton("修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        SQLiteDatabase db = userdatabase.getWritableDatabase();
                        ContentValues values = new ContentValues();

                        values.put("title", input.getText().toString());
                        db.update(userPhotoTable, values, "id = ?", new String[]{beanID});
                        db.close();
                        initDB();

                        photoAdapter.notifyDataSetChanged();
                        seledRowIndex = -1;
                        editMenu.setEnabled(false);
                        }
                });

                builder.setNegativeButton("返回", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            seledRowIndex = -1;
                            editMenu.setEnabled(false);

                            photoAdapter.notifyDataSetChanged();
                            dialog.cancel();
                            }

                });

                builder.setTitle("修改相册名称");
                builder.setView(input);
                builder.show();
                return true;

            case R.id.menu_item_exit:
                ActivityCollector.finishAll();
                return true;
            default:
                return  super.onOptionsItemSelected(item);
        }
    }

    protected class PhotoAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater  layoutInflater;

        public PhotoAdapter(Context context){
            this.context = context;
            this.layoutInflater = LayoutInflater. from(context);
        }
        @Override
        public int getCount() {
            return  photoList.size();
        }
        @Override
        public Object getItem(int position) {
            return  photoList.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {

            if (view ==  null) {
                LinearLayout layout = (LinearLayout) layoutInflater.inflate(R.layout.activity_main_listview_row, null);
                view = layout;
            }

            ImageView thumbView = (ImageView) view.findViewById(R.id.imageViewThumb );
            TextView titleView = (TextView) view.findViewById(R.id.textViewTitle );
            TextView titleViewAddr = (TextView) view.findViewById(R.id.textViewADDR );

            final RowInfoBean bean =  photoList.get(position);
            if (bean != null){
                thumbView.setBackgroundDrawable(bean.thumb);
                titleView.setText(bean.title);
                titleViewAddr.setText(bean.addr);
            }


            if (seledRowIndex == position) {
                view.setBackgroundColor(Color. parseColor ( "#63B8FF"));
            }else {
                view.setBackgroundColor(Color. parseColor("#F0F8FF"));
            }

            ImageView imageViewMap = (ImageView) view.findViewById(R.id.imageViewMap);
            imageViewMap.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Intent intentMap = new Intent(MapPhotoGE.this, MapViewActivity.class);
                    intentMap.putExtra("userID", bean.id);
                   // intentMap.putExtra("thumb", bean.title);
                    intentMap.putExtra("database", userPhotoTable);

                    startActivityForResult(intentMap, REQUEST_MAPVIEW);

                }
            });


            return view;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_MAPVIEW:
                // 从 MapViewActivity 返回则重新加载相册条目
                if (resultCode == RESULT_OK)
                initDB();
                photoAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
