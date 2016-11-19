package com.runhuaoil.mapphotosrunhua;

import android.os.Bundle;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.runhuaoil.mapphotosrunhua.util.CommonUtils;
/**
 * Created by Administrator on 2016/1/3.
 */
public class GalleryActivity extends  BaseActivity {


    private LinearLayout  gallery; //底部显示照片缩略图的“图库”
    private ImageView  pictureView; //显示照片的组件
    private String userTable;
    private  MyUserDataHelper userdatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        //初始化界面组件
        gallery = (LinearLayout) findViewById(R.id.gallery );
        pictureView = (ImageView) findViewById(R.id.imageview_picture );

        userTable = getIntent().getStringExtra("userDataTable");
        userdatabase = new MyUserDataHelper(getApplicationContext(),"UserDataBase",null,1);


        getAllPicture();
    }


    private View getImageView(final String path) {
        int width = dip2px(80);
        int height = dip2px(80);
// 从照片解码 80x80 的缩略图
        Bitmap bitmap = CommonUtils. decodeBitmapFromFile (path, width, height);
//创建工 mageView 组件以显示缩略图，这个组件将被加到底 7nl}的线性布局中
        ImageView imageView = new ImageView( this);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        imageView.setScaleType(ImageView.ScaleType. CENTER_CROP );
        imageView.setImageBitmap(bitmap);
//将 ImageView 力入到 LirearLayout 中
        LinearLayout layout = new LinearLayout( this);
        layout.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        layout.setGravity(Gravity. CENTER );
        layout.addView(imageView);
//点击缩略图则显示对应的照片
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int w =  pictureView.getWidth();
                int h =  pictureView.getHeight();
                Bitmap picture = CommonUtils. decodeBitmapFromFile (path, w, h);
                pictureView.setScaleType(ImageView.ScaleType. CENTER_CROP );

                pictureView.setImageBitmap(picture);
            }
        });
// 设置右边距
        layout.setPadding(0, 0, dip2px(5), 0);
        return layout;
    }


    private void getAllPicture() {
//检查存储卡是否有效
        if(!(Environment. MEDIA_MOUNTED .equals(Environment. getExternalStorageState ()))){
            return;
        }
//从数据库中获取所有拍照的照片文件名
        SQLiteDatabase db = userdatabase.getWritableDatabase();

        Cursor cursor = db.query(userTable, null, null, null,null,null,null);
        while (cursor.moveToNext()) {
            String picture = cursor.getString(cursor.getColumnIndex("picture"));
            String path = CommonUtils. PICTURE_PATH + picture;
// 获取照片图像并创建缩略图 View 寸象，然后 7f1 入到 gallery 布局中
            gallery.addView(getImageView(path));
        }
        cursor.close();
        db.close();
    }


    private  int dip2px( float dip) {
         final  float scale = getResources().getDisplayMetrics(). density;
         return ( int) (dip * scale + 0.5f);
    }
}
