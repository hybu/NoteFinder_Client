package com.app.demos.ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.app.demos.R;
import com.app.demos.base.BaseHandler;
import com.app.demos.base.BaseMessage;
import com.app.demos.base.BaseTask;
import com.app.demos.base.BaseUi;
import com.app.demos.base.BaseUiAuth;
import com.app.demos.base.C;
import com.app.demos.list.BlogList;
import com.app.demos.model.Blog;
import com.app.demos.sqlite.BlogSqlite;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerDragListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class UiMapIndex extends BaseUiAuth {

	private ListView blogListView;
	private BlogList blogListAdapter;
	private BlogSqlite blogSqlite;

	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private Marker mMarkerMe;
	private ArrayList<Marker> mShowBlogs = new ArrayList<Marker>();
	private ArrayList<Blog> mBlogs = new ArrayList<Blog>();
	private InfoWindow mInfoWindow;

	BitmapDescriptor bd = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_gcoding);

	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	BitmapDescriptor mCurrentMarker;

	// MapView mMapView;
	// BaiduMap mBaiduMap;

	// UI相关
	// OnCheckedChangeListener radioButtonListener;
	// Button requestLocButton;
	boolean isFirstLoc = true;// 是否首次定位
	boolean isFirstTouch = false;
	LatLng llMeStart;
	ImageButton mIBBox;
	ImageButton mIBPersonal;
	ImageButton mIBChampion;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_map_index);

		// set handler
		this.setHandler(new IndexHandler(this));

		// init sqlite
		blogSqlite = new BlogSqlite(this);
		

		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMyLocationEnabled(true);

		
		View zoom = mMapView.getChildAt(2);
		zoom.setVisibility(View.GONE);

		// MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
		// mBaiduMap.setMapStatus(msu);
		mCurrentMode = LocationMode.FOLLOWING;
		mCurrentMarker = null;

		mLocClient = new LocationClient(getApplicationContext());
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		option.setNeedDeviceDirect(true);
		mLocClient.setLocOption(option);
		// mLocClient.setAK("rymNGIImRXRNVuDcmNHbOOsW");
		mLocClient.start();

		mIBBox = (ImageButton) this.findViewById(R.id.map_tab_2);

		OnClickListener btnClickListener = new OnClickListener() {
			public void onClick(View v) {
				/*
				 * switch (mCurrentMode) { case NORMAL: mCurrentMode =
				 * LocationMode.FOLLOWING; mBaiduMap
				 * .setMyLocationConfigeration(new MyLocationConfiguration(
				 * mCurrentMode, true, mCurrentMarker)); break; case COMPASS:
				 * mCurrentMode = LocationMode.NORMAL; mBaiduMap
				 * .setMyLocationConfigeration(new MyLocationConfiguration(
				 * mCurrentMode, true, mCurrentMarker)); break; case FOLLOWING:
				 */
				mCurrentMode = LocationMode.COMPASS;
				mBaiduMap
						.setMyLocationConfigeration(new MyLocationConfiguration(
								mCurrentMode, true, mCurrentMarker));
				if (mMarkerMe != null) mMarkerMe.remove();
				mMarkerMe = null;
				/*
				 * break; }
				 */
				MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(20.0f);
				mBaiduMap.setMapStatus(msu);
				isFirstTouch = true;
			}
		};
		mIBBox.setOnClickListener(btnClickListener);

		mIBPersonal = (ImageButton) this.findViewById(R.id.map_tab_1);

		OnClickListener btnClickListener1 = new OnClickListener() {
			public void onClick(View v) {
				HashMap<String, String> blogParams = new HashMap<String, String>();
				blogParams.put("typeId", "0");
				blogParams.put("pageId", "0");
				UiMapIndex.this.doTaskAsync(C.task.blogList, C.api.blogList, blogParams);
			}
		};
		mIBPersonal.setOnClickListener(btnClickListener1);
		
		mIBChampion = (ImageButton) this.findViewById(R.id.map_tab_3);

		OnClickListener btnClickListener3 = new OnClickListener() {
			public void onClick(View v) {
				forward(UiIndex.class);
			}
		};
		mIBChampion.setOnClickListener(btnClickListener3);

		TextView title = (TextView) this.findViewById(R.id.map_top_title);
		title.setText(customer.getName());

		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			public boolean onMarkerClick(final Marker marker) {
				if (mShowBlogs.contains(marker)) {
					int i = mShowBlogs.indexOf(marker);
					Bundle params = new Bundle();
					if (i >= mBlogs.size()) {
						mBaiduMap.hideInfoWindow();
						return true;
					}
					params.putString("blogId", mBlogs.get(i).getId());
					overlay(UiBlog.class, params);
					mBaiduMap.hideInfoWindow();
					return true;
				}
				Button button = new Button(getApplicationContext());
				button.setBackgroundResource(R.drawable.popup);
				final LatLng ll = marker.getPosition();
				Point p = mBaiduMap.getProjection().toScreenLocation(ll);
				p.y -= 47;
				LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);
				OnInfoWindowClickListener listener = null;
				if (marker == mMarkerMe) {
					button.setText("写纸条");
					listener = new OnInfoWindowClickListener() {
						public void onInfoWindowClick() {
							/*
							 * LatLng llNew = new LatLng(ll.latitude + 0.005,
							 * ll.longitude + 0.005); marker.setPosition(llNew);
							 */
							doEditBlog();
							mBaiduMap.hideInfoWindow();
						}
					};
				} /*
				 * else if (marker == mMarkerB) { button.setText("更改图标");
				 * listener = new OnInfoWindowClickListener() { public void
				 * onInfoWindowClick() { marker.setIcon(bd);
				 * mBaiduMap.hideInfoWindow(); } }; } else if (marker ==
				 * mMarkerC) { button.setText("删除"); listener = new
				 * OnInfoWindowClickListener() { public void onInfoWindowClick()
				 * { marker.remove(); mBaiduMap.hideInfoWindow(); } }; }
				 */
				mInfoWindow = new InfoWindow(button, llInfo, listener);
				mBaiduMap.showInfoWindow(mInfoWindow);
				return true;
			}
		});
	}

	public void initOverlay(LatLng llme) {
		// add marker overlay

		OverlayOptions ooMe = new MarkerOptions().position(llme).icon(bd)
				.zIndex(0).draggable(true);// /////////////TODO:zindex
		if (mMarkerMe == null) {
			mMarkerMe = (Marker) (mBaiduMap.addOverlay(ooMe));
		}
		// add ground overlay
		/*
		 * LatLng southwest = new LatLng(39.92235, 116.380338); LatLng northeast
		 * = new LatLng(39.947246, 116.414977); LatLngBounds bounds = new
		 * LatLngBounds.Builder().include(northeast)
		 * .include(southwest).build();
		 * 
		 * OverlayOptions ooGround = new GroundOverlayOptions()
		 * .positionFromBounds(bounds).image(bdGround).transparency(0.8f);
		 * mBaiduMap.addOverlay(ooGround);
		 */

		/*
		 * MapStatusUpdate u = MapStatusUpdateFactory
		 * .newLatLng(bounds.getCenter()); mBaiduMap.setMapStatus(u);
		 */

		mBaiduMap.setOnMarkerDragListener(new OnMarkerDragListener() {
			public void onMarkerDrag(Marker marker) {
			}

			public void onMarkerDragEnd(Marker marker) {
				/*
				 * Toast.makeText( OverlayDemo.this, "拖拽结束，新位置：" +
				 * marker.getPosition().latitude + ", " +
				 * marker.getPosition().longitude, Toast.LENGTH_LONG).show();
				 */
				double dis = Math.abs(marker.getPosition().latitude
						- llMeStart.latitude)
						+ Math.abs(marker.getPosition().longitude
								- llMeStart.longitude);
				if (dis > 0.0007) {
					marker.setPosition(llMeStart);
					toast("目标地点过远，再走近些！");
				}
				customer.setLatitude(marker.getPosition().latitude);
				customer.setLongitude(marker.getPosition().longitude);

			}

			public void onMarkerDragStart(Marker marker) {

			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		//blogSqlite.upgradeTable();
		// show all blog list
		//HashMap<String, String> blogParams = new HashMap<String, String>();
		//blogParams.put("typeId", "0");
		//blogParams.put("pageId", "0");
		//this.doTaskAsync(C.task.blogList, C.api.blogList, blogParams);

	}

	// //////////////////////////////////////////////////////////////////////////////////////////////
	// async task callback methods

	@Override
	public void onTaskComplete(int taskId, BaseMessage message) {
		super.onTaskComplete(taskId, message);

		switch (taskId) {
		case C.task.blogList:
			try {
				@SuppressWarnings("unchecked")
				final ArrayList<Blog> blogList = (ArrayList<Blog>) message
						.getResultList("Blog");
				mBlogs.clear();
				mBlogs = (ArrayList<Blog>)blogList;
				initMyBlogs(blogList);
				
				// load face image
				for (Blog blog : blogList) {
					loadImage(blog.getFace());
					blogSqlite.updateBlog(blog);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				toast(e.getMessage());
			}
			break;
		}
	}

	public void initMyBlogs(ArrayList<Blog> blogList) {
		if (blogList.isEmpty()) {
			toast("没有找到纸条");
			return;
		}
		mShowBlogs.clear();
		for (Blog tmp : blogList) {
			OverlayOptions ooMe = new MarkerOptions().position(new LatLng(Double.valueOf(tmp.getLatitude()),
					Double.valueOf(tmp.getLongitude()))).icon(bd)
					.zIndex(0).draggable(false);// /////////////TODO:zindex
			mShowBlogs.add((Marker) (mBaiduMap.addOverlay(ooMe)));
			
			// add ground overlay
			/*
			 * LatLng southwest = new LatLng(39.92235, 116.380338); LatLng
			 * northeast = new LatLng(39.947246, 116.414977); LatLngBounds
			 * bounds = new LatLngBounds.Builder().include(northeast)
			 * .include(southwest).build();
			 * 
			 * OverlayOptions ooGround = new GroundOverlayOptions()
			 * .positionFromBounds(bounds).image(bdGround).transparency(0.8f);
			 * mBaiduMap.addOverlay(ooGround);
			 */

			/*
			 * MapStatusUpdate u = MapStatusUpdateFactory
			 * .newLatLng(bounds.getCenter()); mBaiduMap.setMapStatus(u);
			 */

//			mBaiduMap.setOnMarkerDragListener(new OnMarkerDragListener() {
//				public void onMarkerDrag(Marker marker) {
//				}
//
//				public void onMarkerDragEnd(Marker marker) {
//					/*
//					 * Toast.makeText( OverlayDemo.this, "拖拽结束，新位置：" +
//					 * marker.getPosition().latitude + ", " +
//					 * marker.getPosition().longitude,
//					 * Toast.LENGTH_LONG).show();
//					 */
//					double dis = Math.abs(marker.getPosition().latitude
//							- llMeStart.latitude)
//							+ Math.abs(marker.getPosition().longitude
//									- llMeStart.longitude);
//					if (dis > 0.0007) {
//						marker.setPosition(llMeStart);
//						toast("目标地点过远，再走近些！");
//					}
//					customer.setLatitude(marker.getPosition().latitude);
//					customer.setLongitude(marker.getPosition().longitude);
//
//				}
//
//				public void onMarkerDragStart(Marker marker) {
//
//				}
//			});
		}
	}

	
	@Override
	public void onNetworkError(int taskId) {
		super.onNetworkError(taskId);
		toast(C.err.network);
		switch (taskId) {
		case C.task.blogList:
			try {
				final ArrayList<Blog> blogList = blogSqlite.getAllBlogs();
				// load face image
				for (Blog blog : blogList) {
					loadImage(blog.getFace());
					blogSqlite.updateBlog(blog);
				}
				// show text
				// blogListView = (ListView)
				// this.findViewById(R.id.app_index_list_view);
				//blogListAdapter = new BlogList(this, blogList);
				// blogListView.setAdapter(blogListAdapter);
				// blogListView.setOnItemClickListener(new
				// OnItemClickListener(){
				// @Override
				// public void onItemClick(AdapterView<?> parent, View view, int
				// pos, long id) {
				// Bundle params = new Bundle();
				// params.putString("blogId", blogList.get(pos).getId());
				// overlay(UiBlog.class, params);
				// }
				// });
			} catch (Exception e) {
				e.printStackTrace();
				toast(e.getMessage());
			}
			break;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////
	// other methods

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			doFinish();
		}
		return super.onKeyDown(keyCode, event);
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////
	// inner classes

	private class IndexHandler extends BaseHandler {
		public IndexHandler(BaseUi ui) {
			super(ui);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try {
				switch (msg.what) {
				case BaseTask.LOAD_IMAGE:
					//blogListAdapter.notifyDataSetChanged();
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				ui.toast(e.getMessage());
			}
		}
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
					.direction(360).latitude(location.getLatitude()) // ///////////TODO
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
			mCurrentMode = LocationMode.NORMAL;
			mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
					mCurrentMode, true, mCurrentMarker));
			llMeStart = new LatLng(location.getLatitude(),
					location.getLongitude());
			initOverlay(new LatLng(location.getLatitude(),
					location.getLongitude()));
			customer.setSetLocation(true);
			customer.setLatitude(location.getLatitude());
			customer.setLongitude(location.getLongitude());

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
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
	}
}