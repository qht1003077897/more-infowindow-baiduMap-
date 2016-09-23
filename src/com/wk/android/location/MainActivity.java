package com.wk.android.location;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.inner.Point;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.map.B;
import com.baidu.platform.comjni.tools.ParcelItem;

public class MainActivity extends Activity {
 
	private LocationClient locationClient;
	private BDLocationListener listener = new MyLocationListener();
	private MapView mapView;
	private BaiduMap baiduMap;
	
	private LinkedList<LocationEntity> locationList = new LinkedList<LocationEntity>();
	
	private boolean toastLoad = false;
	
	private TextView infoText;
	private ProgressBar pb;
	
	private double longitude;
	private double latitude;
	
	String LocationDescribe;
	
	private boolean flag=false;//是否点击
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toast.makeText(this, "正在定位...", Toast.LENGTH_SHORT).show();
		locationClient = new LocationClient(getApplicationContext());
		locationClient.registerLocationListener(listener);
		initLocation();
		mapView = (MapView) findViewById(R.id.bmapView);
		baiduMap = mapView.getMap();
		baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(17));//地图缩放级别为17
		infoText = (TextView) findViewById(R.id.location_info);
		pb = (ProgressBar) findViewById(R.id.load_view);
//        initPoint();
	baiduMap.setOnMapClickListener(new OnMapClickListener() {
			
			@Override
			public boolean onMapPoiClick(MapPoi poi) {
				baiduMap.clear();
				initPointandMarker();
				LatLng point=poi.getPosition();
				showPop(point,null,poi);
				return false;
			}
			
			@Override
			public void onMapClick(LatLng point) {
				Log.d("map click", point.longitude+"，"+point.latitude);
//				setMapOverlay(point);
				baiduMap.clear();
				initPointandMarker();
//				showPop(point,null,null);
				flag=true;
//			String aa=	locationList.getLast().location.getPoiList().get(0).toString();
				getInfoFromLAL(point);
				
			}
			
		});
	//mark的点击事件
	baiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
		
		@Override
		public boolean onMarkerClick(Marker mark) {
			// TODO Auto-generated method stub
			baiduMap.clear();
			initPointandMarker();
			return true;
		}
	});
	}
	//初始化点集合,顺序为从左上角顺时针
	private void initPointandMarker() {
		// TODO Auto-generated method stub

		LatLng point1 = new LatLng(34.26406830033401,108.9946414056412);
		LatLng point2 = new LatLng(34.26396387964535,109.0030315791044);
		LatLng point3 = new LatLng(34.25697485470486,109.0030315791044);
		LatLng point4 = new LatLng(34.2571464172961,108.99461445647592);
		List<pointAddInfoModel> points=new ArrayList<pointAddInfoModel>();
		pointAddInfoModel infoModel1=new pointAddInfoModel();
		pointAddInfoModel infoModel2=new pointAddInfoModel();
		pointAddInfoModel infoModel3=new pointAddInfoModel();
		pointAddInfoModel infoModel4=new pointAddInfoModel();
		infoModel1.setPoints(point1);//for循环去存储
		infoModel1.setInfo("一");
		points.add(infoModel1);
		infoModel2.setPoints(point2);
		infoModel2.setInfo("二");
		points.add(infoModel2);
		infoModel3.setPoints(point3);
		infoModel3.setInfo("三");
		points.add(infoModel3);
		infoModel4.setPoints(point4);
		infoModel4.setInfo("四");
		points.add(infoModel4);
		showPopAndMarker(points);
	}

	// 初始化定位参数
	private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(true);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        locationClient.setLocOption(option);
        locationClient.start();
    }

	// 定位监听
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation loc) {
			if (loc != null) {
				Bundle locData = algorithm(loc);
				Message locMsg = locHander.obtainMessage();
				if (locData != null) {
					locData.putParcelable("loc", loc);
					locMsg.setData(locData);
					locHander.sendMessage(locMsg);
					
					if (!toastLoad) {
						Toast.makeText(MainActivity.this, "正在加载地图...", Toast.LENGTH_SHORT).show();
					}
					toastLoad = true;
					locationClient.stop();
				}
			} else {
				Toast.makeText(MainActivity.this, "定位失败，请检查手机网络或设置！", Toast.LENGTH_LONG).show();
			}
		}
		
	}
	
	/***
	 * 接收定位结果消息，并显示在地图上（刚开始定位初始化一次）
	 */
	private Handler locHander = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try {
				BDLocation location = msg.getData().getParcelable("loc");
				
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				
				Log.d("time",location.getTime());
				Log.d("error code", location.getLocType()+"");
				Log.d("latitude", location.getLatitude()+"");
				Log.d("longitude", location.getLongitude()+"");
				Log.d("radius", location.getRadius()+"");
				if (location.getLocType() == BDLocation.TypeGpsLocation) {
					Log.d("GPS", "gps定位成功");
					Log.d("speed", location.getSpeed()+" 单位：公里每小时");
					Log.d("satellite", location.getSatelliteNumber()+"");
					Log.d("height", location.getAltitude()+" 单位：米");
					Log.d("direction", location.getDirection()+" 单位度");
					Log.d("addr", location.getAddrStr());
				} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
					Log.d("网络定位结", "网络定位成功");
					Log.d("addr", location.getAddrStr());
					Log.d("operationers", "运营商信息："+location.getOperators());
				} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
					Log.d("离线定位", "离线定位成功，离线定位结果也是有效的");
				} else if (location.getLocType() == BDLocation.TypeServerError) {
					Toast.makeText(MainActivity.this, "服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因", Toast.LENGTH_LONG).show();
				} else if (location.getLocType() == BDLocation.TypeNetWorkException) {
					Toast.makeText(MainActivity.this, "网络不同导致定位失败，请检查网络是否通畅", Toast.LENGTH_LONG).show();
				} else if (location.getLocType() == BDLocation.TypeCriteriaException) {
					Toast.makeText(MainActivity.this, "无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机", Toast.LENGTH_LONG).show();
				}
				Log.d("locationdescribe", location.getLocationDescribe());
				
				List<Poi> list = location.getPoiList();// POI数据
				StringBuffer sb = new StringBuffer("POI数据：");
				if (list != null && list.size() > 0) {
					sb.append("\npoilist size = : ");
	                sb.append(list.size());
	                for (Poi p : list) {
	                    sb.append("\npoi= : ");
	                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
	                }
				}
				
				Log.d("POI数据", sb.toString());
				 LocationDescribe=location.getLocationDescribe();
				infoText.setText("经度："+location.getLongitude()+"，纬度"+location.getLatitude()
						+"\n"+location.getAddrStr()
						+"\n"+location.getLocationDescribe());
				
				
				if (location != null) {
					LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
					setMapOverlay(point);
					showPop(point, "您的位置", null);
					baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(point));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	};
	
	/***
	 * 平滑策略代码实现方法，主要通过对新定位和历史定位结果进行速度评分，
	 * 来判断新定位结果的抖动幅度，如果超过经验值，则判定为过大抖动，进行平滑处理,若速度过快，
	 * 则推测有可能是由于运动速度本身造成的，则不进行低速平滑处理
	 * 
	 * @param BDLocation
	 * @return Bundle
	 */
	private Bundle algorithm(BDLocation location) {
		float[] EARTH_WEIGHT = {0.1f,0.2f,0.4f,0.6f,0.8f}; // 推算计算权重_地球
		Bundle locData = new Bundle();
		double curSpeed = 0;
		if (locationList.isEmpty() || locationList.size() < 2) {
			LocationEntity temp = new LocationEntity();
			temp.location = location;
			temp.time = System.currentTimeMillis();
			locData.putInt("iscalculate", 0);
			locationList.add(temp);
		} else {
			if (locationList.size() > 5)
				locationList.removeFirst();
			double score = 0;
			for (int i = 0; i < locationList.size(); ++i) {
				LatLng lastPoint = new LatLng(locationList.get(i).location.getLatitude(),
						locationList.get(i).location.getLongitude());
				LatLng curPoint = new LatLng(location.getLatitude(), location.getLongitude());
				double distance = DistanceUtil.getDistance(lastPoint, curPoint);
				curSpeed = distance / (System.currentTimeMillis() - locationList.get(i).time) / 1000;
				score += curSpeed * EARTH_WEIGHT[i];
			}
			if (score > 0.00000999 && score < 0.00005) {
				location.setLongitude(
						(locationList.get(locationList.size() - 1).location.getLongitude() + location.getLongitude())
								/ 2);
				location.setLatitude(
						(locationList.get(locationList.size() - 1).location.getLatitude() + location.getLatitude())
								/ 2);
				locData.putInt("iscalculate", 1);
			} else {
				locData.putInt("iscalculate", 0);
			}
			LocationEntity newLocation = new LocationEntity();
			newLocation.location = location;
			newLocation.time = System.currentTimeMillis();
			locationList.add(newLocation);

		}
		return locData;
	}
	
	class LocationEntity {
		BDLocation location;
		long time;
	}
	
	// 在地图上添加标注
	private void setMapOverlay(LatLng point) {
		latitude = point.latitude;
		longitude = point.longitude;
		
		baiduMap.clear();
		initPointandMarker();
		BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_openmap_mark);
		OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
		baiduMap.addOverlay(option);
		
	}
	
	
	
	public void showPop(LatLng point,String Address,MapPoi poi) {  
	    // 创建InfoWindow展示的view  
	    View popup = View.inflate(this, R.layout.pop, null);  
//	    TextView title = (TextView) popup.findViewById(R.id.tv_title);  
	    TextView content = (TextView) popup.findViewById(R.id.tv_content); 
//	    title.setText(name);  
	  if(poi==null||poi.getName()==null||"".equals(poi.getName())){
		  content.setText(Address); 
	  }else{
		  content.setText(poi.getName()); 
	  }
//	    // 创建InfoWindow的点击事件监听者  
//	    OnInfoWindowClickListener listener = new OnInfoWindowClickListener() {  
//	        public void onInfoWindowClick() {  
//	            // 添加点击后的事件响应代码  
//	            Uri uri = Uri.parse("geo:" + latitude + "," + longitude + "");  
//	            Intent it = new Intent(Intent.ACTION_VIEW, uri);  
//	            startActivity(it);  
//	        }  
//	    };  
	 
	    // 创建InfoWindow  
	    InfoWindow infoWindow=new InfoWindow(popup, point,0);
	    // 显示InfoWindow  
	    baiduMap.showInfoWindow(infoWindow);  
	} 
//	初始化时显示固定经纬度的提示框
	public void showPopAndMarker(List<pointAddInfoModel> points) {  
		 // 创建InfoWindow展示的view  
//	    View popup1 = View.inflate(this, R.layout.pop, null);  
////	    TextView title = (TextView) popup.findViewById(R.id.tv_title);  
//	    TextView content1 = (TextView) popup1.findViewById(R.id.tv_content); 
		BitmapDescriptor bitmap;
		
		for(int i=0;i<points.size();i++){
	        Bitmap bitmap3 = null;
			Bitmap bitmap4 = null; 
				try {
					 bitmap3 =drawbitmap();
					 bitmap4 =drawtext(bitmap3,points.get(i).getInfo());
			        bitmap = BitmapDescriptorFactory.fromBitmap(bitmap4);
					OverlayOptions option = new MarkerOptions().position(points.get(i).getPoint()).icon(bitmap);
					baiduMap.addOverlay(option);
				}
        finally{
			        		bitmap3.recycle();
							bitmap4.recycle();
							bitmap3=null;
							bitmap4=null;
        }
		}
	} 
	
	private Bitmap drawtext(Bitmap bitmap3,String info) {
	// TODO Auto-generated method stub
		 int width = bitmap3.getWidth(), hight = bitmap3.getHeight();
	       Bitmap icon = Bitmap.createBitmap(width, hight, Bitmap.Config.ARGB_8888); //建立一个空的BItMap  
	       Canvas canvas = new Canvas(icon);//初始化画布绘制的图像到icon上  
	       Paint photoPaint = new Paint(); //建立画笔  
	       photoPaint.setDither(true); //获取跟清晰的图像采样  
	       photoPaint.setFilterBitmap(true);//过滤一些  
	       Rect src = new Rect(0, 0, bitmap3.getWidth(), bitmap3.getHeight());//创建一个指定的新矩形的坐标  
	       Rect dst = new Rect(0, 0, width, hight);//创建一个指定的新矩形的坐标  
	       canvas.drawBitmap(bitmap3, src, dst, photoPaint);//将photo 缩放或则扩大到 dst使用的填充区photoPaint  
	       Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);//设置画笔  
	       textPaint.setTextSize(28.0f);//字体大小  
	       textPaint.setTypeface(Typeface.DEFAULT_BOLD);//采用默认的宽度  
	       textPaint.setColor(Color.BLACK);//采用的颜色  
	       //textPaint.setShadowLayer(3f, 1, 1,this.getResources().getColor(android.R.color.background_dark));//影音的设置  
	       canvas.drawText(info, 23, 32, textPaint);//绘制上去字，开始未知x,y采用那只笔绘制 
	       canvas.save(Canvas.ALL_SAVE_FLAG); 
	       canvas.restore(); 
	       return icon;
}
	private Bitmap drawbitmap() {
	// TODO Auto-generated method stub
		Bitmap photo = BitmapFactory.decodeResource(this.getResources(),R.drawable.info_bubble);
		int width = photo.getWidth();
		int hight = photo.getHeight();
		Bitmap newb = Bitmap.createBitmap(width, hight, Config.ARGB_8888);
		Canvas canvas = new Canvas(newb);// 初始化和方框一样大小的位图
		Paint photoPaint = new Paint(); // 建立画笔
		canvas.drawBitmap(photo, 0, 0, photoPaint);
		canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.restore();
			return newb;
}
	// 根据经纬度查询位置
	private void getInfoFromLAL(final LatLng point) {
		pb.setVisibility(View.VISIBLE);
		infoText.setText("经度："+point.longitude+"，纬度"+point.latitude);
		GeoCoder gc = GeoCoder.newInstance();
		gc.reverseGeoCode(new ReverseGeoCodeOption().location(point));
		gc.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
			
			@Override
			public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
				pb.setVisibility(View.GONE);
				if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
					Log.e("发起反地理编码请求", "未能找到结果");
				} else {
						showPop(point,result.getAddress(),null);
					infoText.setText("经度："+point.longitude+"，纬度:"+point.latitude
							+"\n"+result.getAddress());
				}
			}
			
			@Override
			public void onGetGeoCodeResult(GeoCodeResult result) {
				
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}
}
