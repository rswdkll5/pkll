package com.example.jkbaidumap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class MainActivity extends Activity {
	// int count=-0;
	MapView mMapView = null;
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	BitmapDescriptor mCurrentMarker;
	// double distance=0;

	BaiduMap mBaiduMap;
	private Timer timer = new Timer();
	private TimerTask task;
	// List<LatLng> points = new ArrayList<LatLng>();
	List<LatLng> pointstwo = new ArrayList<LatLng>();
	LatLng p1;
	LatLng p2;
	// UI相关
	OnCheckedChangeListener radioButtonListener;
	Button requestLocButton;
	boolean isFirstLoc = true;// 是否首次定位
	

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			// 调用相机回调接口由于MainActivity已经实现了回调接口，所以MainActivity.this即可

			if (msg.what == 1) {
				// if(points.size()>3){
				// pointstwo.add((LatLng)points.get(points.size()-3));
				// pointstwo.add((LatLng)points.get(points.size()-2));
				// pointstwo.add((LatLng)points.get(points.size()-1));
				// }else{
				// pointstwo.add((LatLng)points.get(count));
				// }
				// Log.d("points",""+points.get(points.size()-3).latitude);
				// Log.d("points",""+points.get(points.size()-2).latitude);
				// Log.d("points",""+points.get(points.size()-1).latitude);
				pointstwo.add(p1);
				pointstwo.add(p2);
				// distance+=DistanceUtil. getDistance(p1, p2);
				// LatLng llDot = new LatLng(p2.latitude,p2.longitude);
			//	try{
				OverlayOptions ooDot = new DotOptions().center(p2).radius(6)
						.color(0xAAFF0000);
				mBaiduMap.addOverlay(ooDot);

				OverlayOptions ooPolyline = new PolylineOptions().width(4)
						.color(0xAAFF0000).points(pointstwo);
				mBaiduMap.addOverlay(ooPolyline);
				p1 = p2;
				mLocClient.requestLocation();
			//	}catch(Exception e){
				//	e.printStackTrace();
			//	}

			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		/*
		 * forbid lock screen
		 */
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		
		// 获取地图控件引用
		Log.d("points", "wwww");

		requestLocButton = (Button) findViewById(R.id.button1);
		mCurrentMode = LocationMode.NORMAL;
		requestLocButton.setText("普通");
		OnClickListener btnClickListener = new OnClickListener() {
			public void onClick(View v) {
				switch (mCurrentMode) {
				case NORMAL:
					requestLocButton.setText("跟随");
					mCurrentMode = LocationMode.FOLLOWING;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case COMPASS:
					requestLocButton.setText("普通");
					mCurrentMode = LocationMode.NORMAL;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case FOLLOWING:
					requestLocButton.setText("罗盘");
					mCurrentMode = LocationMode.COMPASS;

					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				}
			}
		};
		requestLocButton.setOnClickListener(btnClickListener);

		RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup);
		radioButtonListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.defaulticon) {
					// 传入null则，恢复默认图标
					mCurrentMarker = null;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, null));
				}
				if (checkedId == R.id.customicon) {
					// 修改为自定义marker
					mCurrentMarker = BitmapDescriptorFactory
							.fromResource(R.drawable.icon_geo);
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
				}
			}
		};
		group.setOnCheckedChangeListener(radioButtonListener);

		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);

		// 开启交通图
		mBaiduMap.setTrafficEnabled(true);
		mBaiduMap.setMapStatus(MapStatusUpdateFactory
				.newMapStatus(new MapStatus.Builder().zoom(17).build()));// 设置缩放级别
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setIsNeedAddress(true);
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();
		init();

	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;
				// p1=new LatLng(39.93923, 116.357428);
				p1 = p2 = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(p2);
				// count++;
				// points.add(p2);
				// pointstwo.add(ll);
				mBaiduMap.animateMapStatus(u);
			} else {
				// count++;
				p2 = new LatLng(location.getLatitude(), location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(p2);
				// points.add(p2);
				Log.d("points", "wwww");
				OverlayOptions ooDot = new DotOptions().center(p2).radius(6)
						.color(0xAAFF0000);
				mBaiduMap.addOverlay(ooDot);

				mBaiduMap.animateMapStatus(u);

			}

		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
		startTimer();
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		pauseTimer();
		super.onDestroy();
	}

	public void startTimer() {
		if (timer == null) {
			timer = new Timer();
		}
		if (task == null) {
			task = new TimerTask() {
				@Override
				public void run() {
					// TODO Auto-generated method stub

					Message message = new Message();
					message.what = 1;
					handler.sendMessage(message);

				}
			};
		}
		if (timer != null && task != null) {
			timer.schedule(task, 3000, 1000);
		}
	}

	public void pauseTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (task != null) {
			task.cancel();
			task = null;
		}
	}

	void init() {
		Button bt = (Button) findViewById(R.id.cutpicture);
		bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
			
			  new Thread(new Runnable() {  
                  @Override  
                  public void run() {  
                	  takeScreenShot(mMapView,"/mnt/sdcard/libichen/");
                  }  
              }).start();  
			}

		});
	}

	/**
     * 截屏方法
     * @param view
     * @param path
     * @param fileName
     */
    public static  boolean takeScreenShot(View view ,String path){
            boolean isSucc=false;
            /**
             *  我们要获取它的cache先要通过setDrawingCacheEnable方法把cache开启，
             *  然后再调用getDrawingCache方法就可以获得view的cache图片了。
             *  buildDrawingCache方法可以不用调用，因为调用getDrawingCache方法时，
             *  若果cache没有建立，系统会自动调用buildDrawingCache方法生成cache。
             *  若果要更新cache, 必须要调用destoryDrawingCache方法把旧的cache销毁，
             *  才能建立新的。
             */
            view.setDrawingCacheEnabled(true);//开启获取缓存
            view.buildDrawingCache();
            Bitmap bitmap=view.getDrawingCache();//得到View的cache
            Canvas canvas=new Canvas(bitmap);
            int w=bitmap.getWidth();
            int h=bitmap.getHeight();
            
            Paint paint=new Paint();
            paint.setColor(Color.YELLOW);
            SimpleDateFormat simple=new SimpleDateFormat("yyyyMMddhhmmss");
            String time=simple.format(new Date());
            
            //canvas.drawText(time, w-w/2, h-h/10, paint);
            canvas.save();
            canvas.restore();
            FileOutputStream fos=null;
            try{
                    File sddir=new File(path);
                    if(!sddir.exists()){
                            sddir.mkdir();
                    }
                    //File file = new File("/mnt/sdcard/libichen.png");
                    File file=new File(path+time + ".jpg");
                    fos=new FileOutputStream(file);
                    if(fos!=null){
                            bitmap.compress(Bitmap.CompressFormat.JPEG,100, fos);
                            fos.close();
                            isSucc=true;
                    }
            }catch(Exception e){
                    
                    e.printStackTrace();
            }
            return isSucc;
    
}
    
    
}