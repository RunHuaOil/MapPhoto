package com.runhuaoil.mapphotosrunhua;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.runhuaoil.mapphotosrunhua.util.CommonUtils;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2016/1/2.
 */
public class MapViewActivity extends BaseActivity {


    private double latiude,longitude;
    private  String addr;
    private  MyUserDataHelper userdatabase;
    private BaiduMap baiduMap;
    MapView mMapView = null;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    private ImageView popCamera;
    private ImageView  snap;
    private LinearLayout cameraBar;
    private LinearLayout  previewArea;
    private LinearLayout  snapArea;

    private Camera camera;
    private CameraSurfaceView  cameraSurfaceView;
    private Bitmap picture;

    private String userPhotoTable,id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        id = getIntent().getIntExtra("userID",-1) +"";
        userPhotoTable = getIntent().getStringExtra("database");

        userdatabase = new MyUserDataHelper(getApplicationContext(),"UserDataBase",null,1);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        initLocation();
        mMapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mMapView.getMap();
        mLocationClient.start();

        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                Intent intentGallery = new Intent(MapViewActivity.this, GalleryActivity.class);
                intentGallery.putExtra( "userDataTable", userPhotoTable);
                startActivity(intentGallery);
                return true;
            }
        });


        SQLiteDatabase db1 = userdatabase.getWritableDatabase();
        Cursor cur = db1.query(userPhotoTable, null, "id = ?", new String[]{id}, null, null, null);
        String thumbPhoto = "";
        double loti = 0.0 ,longit = 0.0;

        if (cur.moveToFirst()){
            do  {
                thumbPhoto = cur.getString(cur.getColumnIndex("thumb"));
                loti = cur.getDouble(cur.getColumnIndex("latitude"));
                longit = cur.getDouble(cur.getColumnIndex("longitude"));
            }while(cur.moveToNext());

            if (thumbPhoto != null){

                LatLng pointID = new LatLng(loti, longit);

                String thumbPhotoPATH = CommonUtils.THUMB_PATH + thumbPhoto;

                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromPath(thumbPhotoPATH);
                MarkerOptions option1 = new MarkerOptions()
                        .position(pointID)
                        .icon(bitmap)
                        .zIndex(5);
                option1.animateType(MarkerOptions.MarkerAnimateType.drop);
                if (baiduMap != null){

                    Marker marker = (Marker) baiduMap.addOverlay(option1);
                    marker.setTitle(thumbPhoto);
                }
            }

        }
        cur.close();
        db1.close();


        popCamera = (ImageView) findViewById(R.id. popCamera );
        cameraBar = (LinearLayout) findViewById(R.id. cameraBar );
        previewArea = (LinearLayout) findViewById(R.id. previewArea );
        snapArea = (LinearLayout) findViewById(R.id. snapArea );
        snap = (ImageView) findViewById(R.id. snap );

        cameraBar.setVisibility(View.INVISIBLE);


        popCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (cameraBar.getVisibility() == View.VISIBLE) {
                    cameraBar.removeAllViews();
                    cameraBar.setVisibility(View.INVISIBLE);
                } else if (cameraBar.getVisibility() == View.INVISIBLE) {
                    if (cameraSurfaceView == null) {
                        cameraSurfaceView = new CameraSurfaceView(getApplicationContext());
                        // 设置取景预览画面置顶显示，否则将被地图覆盖件
                        cameraSurfaceView.setZOrderOnTop(true);
                        // 将 camera5urfaceView 放进 previewArea 布局中
                        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        previewArea.addView(cameraSurfaceView, param);
                    }
                    // 动态构建相机预览界面(取景预览和拍照)，使之可见
                    cameraBar.removeAllViews();
                    cameraBar.addView(previewArea);
                    cameraBar.addView(snapArea);
                    cameraBar.setVisibility(View.VISIBLE);
                }
            }
        });

        snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(camera !=  null){
                        // 启动相机聚焦拍照
                    camera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            if (success) {
                                camera.takePicture( null,  null, new PictureTakenCallback());
                            }
                            else {
                                Toast.makeText (getApplicationContext(), "警告:相机无法聚焦",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });



    }

    private class PictureTakenCallback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken( byte[] data, Camera camera) {
            // 视情况释放照片内存
            if (picture != null && ! picture.isRecycled()) {
                picture.recycle();
            }
            // 暂停相机预览
            camera.stopPreview();
            picture = BitmapFactory.decodeByteArray(data, 0, data.length);
            // 因为竖屏预览时旋转了 90 度，故照片需往回旋转 90 度

            if (getResources().getConfiguration().orientation == Configuration. ORIENTATION_PORTRAIT ) {
                // 构造旋转矩阵，主要用在图像处理中
                Matrix matrix = new Matrix();
                matrix.postRotate(90); /*翻转 90 度*/
                int w =  picture.getWidth();
                int h =  picture.getHeight();
        // 将照片旋转 90 度
                try {
                    Bitmap tbmp = Bitmap. createBitmap ( picture, 0, 0, w, h, matrix,  true);
                    picture.recycle();
                    picture = tbmp;
                }catch (OutOfMemoryError oom) {
        // rotate failed
                }
            }else {
        // do nothing
            }

            if(picture !=  null){
                // 保存照片文件到 SD 卡
                String picPath = CommonUtils.savePicture(getApplicationContext(), picture, CommonUtils.PICTURE_PATH);
                Bitmap thumb64 = CommonUtils.getPicture64 (picPath);
                // 保存缩略图文件到 SD 卡
                String thumb64Path = CommonUtils.savePicture(getApplicationContext(), thumb64, CommonUtils.THUMB_PATH );
                // 获得照片、缩略图文件名（不含所在的目录）
                String picname = new File(picPath).getName();
                String thumb64name = new File(thumb64Path).getName();
                // 保存照片数据到数据库
                SQLiteDatabase db = userdatabase.getWritableDatabase();
                ContentValues values = new ContentValues();

                Cursor cur1 = db.query(userPhotoTable, null, "id = ?", new String[]{id}, null, null, null);
                String thumb1="";
                if (cur1.moveToFirst()){
                    do  {
                        thumb1 = cur1.getString(cur1.getColumnIndex("thumb"));
                    }while(cur1.moveToNext());

                    if (thumb1 != null){
                        values.put("latitude",latiude);
                        values.put("longitude",longitude);
                        values.put("address", addr);
                        values.put("thumb", thumb64name);
                        values.put("picture",picname);
                        db.insert(userPhotoTable, null, values);
                        values.clear();
                    }else{
                        values.put("latitude",latiude);
                        values.put("longitude",longitude);
                        values.put("address", addr);
                        values.put("thumb", thumb64name);
                        values.put("picture",picname);
                        db.update(userPhotoTable, values, "id = ?", new String[]{id});
                        values.clear();
                    }

                }
                cur1.close();
                db.close();


                LatLng point = new LatLng(latiude, longitude);
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromBitmap(thumb64);
                MarkerOptions option = new MarkerOptions()
                        .position(point)
                        .icon(bitmap)
                        .zIndex(5);
                option.animateType(MarkerOptions.MarkerAnimateType.drop);

                Marker marker = (Marker) baiduMap.addOverlay(option);
                marker.setTitle(picname);

                picture.recycle();
                picture =  null;
                Toast.makeText (getApplicationContext(),  "已拍照", Toast. LENGTH_SHORT ).show();
            }
        //拍照结束继续预览
            camera.startPreview();
        }
    }

    @Override
    protected void onDestroy() {
        if (baiduMap != null){
            baiduMap.setMyLocationEnabled(false);
        }
        mMapView.onDestroy();
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理

    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理

    }

    @Override
    protected void onPause() {

        mMapView.onPause();
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理

    }

    @Override
    protected void onStop() {
        mLocationClient.stop();

        super.onStop();

    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=5000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(true);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
//            StringBuffer sb = new StringBuffer(256);
//            sb.append("time : ");
//            sb.append(location.getTime());
//            sb.append("\nerror code : ");
//            sb.append(location.getLocType());
//            sb.append("\nlatitude : ");
//            sb.append(location.getLatitude());
//            sb.append("\nlontitude : ");
//            sb.append(location.getLongitude());
//            sb.append("\nradius : ");
//            sb.append(location.getRadius());
//            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
//                sb.append("\nspeed : ");
//                sb.append(location.getSpeed());// 单位：公里每小时
//                sb.append("\nsatellite : ");
//                sb.append(location.getSatelliteNumber());
//                sb.append("\nheight : ");
//                sb.append(location.getAltitude());// 单位：米
//                sb.append("\ndirection : ");
//                sb.append(location.getDirection());// 单位度
//                sb.append("\naddr : ");
//                sb.append(location.getAddrStr());
//                sb.append("\ndescribe : ");
//                sb.append("gps定位成功");
//
//            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
//                sb.append("\naddr : ");
//                sb.append(location.getAddrStr());
//                //运营商信息
//                sb.append("\noperationers : ");
//                sb.append(location.getOperators());
//                sb.append("\ndescribe : ");
//                sb.append("网络定位成功");
//            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
//                sb.append("\ndescribe : ");
//                sb.append("离线定位成功，离线定位结果也是有效的");
//            } else if (location.getLocType() == BDLocation.TypeServerError) {
//                sb.append("\ndescribe : ");
//                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
//            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
//                sb.append("\ndescribe : ");
//                sb.append("网络不同导致定位失败，请检查网络是否通畅");
//            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
//                sb.append("\ndescribe : ");
//                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
//            }
//            sb.append("\nlocationdescribe : ");
//            sb.append(location.getLocationDescribe());// 位置语义化信息
//            List<Poi> list = location.getPoiList();// POI数据
//            if (list != null) {
//                sb.append("\npoilist size = : ");
//                sb.append(list.size());
//                for (Poi p : list) {
//                    sb.append("\npoi= : ");
//                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
//                }
//            }

            latiude = location.getLatitude();
            longitude = location.getLongitude();
            addr = location.getAddrStr();
            //Log.i("RunHua", sb.toString());


            baiduMap.setMyLocationEnabled(true);

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(location.getDirection())// 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(location.getLatitude())  // 设置定位数据
                    .longitude(location.getLongitude())
                    .build();

            baiduMap.setMyLocationData(locData);

            //BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher);
            MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, null);
            baiduMap.setMyLocationConfigeration(config);



        }

    }

    private class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
        private SurfaceHolder surfaceHolder =  null;
        public CameraSurfaceView(Context context) {
            super(context);
            // 保存 surfaceHolder,设定回调对象
            surfaceHolder =  this.getHolder();
            surfaceHolder.addCallback(this);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 当界面变化时，暂停取景预览
            camera.stopPreview();
            surfaceHolder = holder;
        // 指定相机参数:图片分辨率，横竖屏切换，自动聚焦
            Camera.Parameters param =  camera.getParameters();
            // 指定拍照图片的大小
            List<Camera.Size> sizes = param.getSupportedPictureSizes();
            Collections. sort(sizes, new Comparator<Camera.Size>() {
                @Override
                public int compare(Camera.Size s1, Camera.Size s2) {
            // 倒排序，确保大的预览分辨率在前
                    return s2.width - s1.width;
                }
            });

            for (Camera.Size size : sizes) {
            // 拍照分辨率不能设置过大，否则会造成 OutOfMemoryException 异常
                if (size.width <= 1200) {
                    param.setPictureSize(size. width, size. height);
                    break;
                }
            }
            // 横竖屏镜头自动调整
            if ( this.getResources().getConfiguration().orientation !=
                    Configuration. ORIENTATION_LANDSCAPE ) {
            // 设置为坚屏取景预览

                param.set( "orientation",  "portrait");
                camera.setDisplayOrientation(90);
            }else {
            // 设置为横屏取景预览
                param.set( "orientation",  "landscape");
                camera.setDisplayOrientation(0);
            }
            // 设置相机为自动聚焦模式
            List<String> focusModes = param.getSupportedFocusModes();

            if (focusModes.contains(Camera.Parameters. FOCUS_MODE_AUTO )) {
                param.setFocusMode(Camera.Parameters. FOCUS_MODE_AUTO );
            }
            // 使参数设置生效
            camera.setParameters(param);
            // 设置相机取景预览的缓冲区内存，取决于画面宽、高和每像素占用的字节数
            int imgformat = param.getPreviewFormat();
            int bitsperpixel = ImageFormat.getBitsPerPixel(imgformat);
            Camera.Size camerasize = param.getPreviewSize();
            int frame_size = ((camerasize.width * camerasize. height) * bitsperpixel) / 8;
            byte[] frame = new  byte[frame_size];
            // 相机取景预览时会将预览画面图像存到 frame 变量中
            camera.addCallbackBuffer(frame);
            camera.setPreviewCallbackWithBuffer(previewCallback);
            // 启动相机取景预览
            camera.startPreview();
        }
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if(camera ==  null){
            // 获取相机服务
                camera = Camera. open ();
            }
            try{
            // 设置预览画面的显示场所
                camera.setPreviewDisplay( surfaceHolder);
            } catch(Exception e){
            // 若连接相机夫败，则释放资源
                camera.release();
                camera =  null;
                e.printStackTrace();
            }

        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // 停止预览，释放系统相机服务
            camera.setPreviewCallback( null); //！！这个必须在前，不然退出出错
            camera.stopPreview();
            camera.release();
            camera =  null;
        }
        /*
        * Camera 取景预览回调接口
        */
        private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback(){
            @Override
            public void onPreviewFrame( byte[] data, Camera camera) {
        // 重复将预览的画面帧图像放到同一个 data 缓冲区中
                camera.addCallbackBuffer(data);
            }
        };
    } //of CameraSurfaceView


    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }
}
