package com.wk.android.location;

import com.baidu.mapapi.SDKInitializer;

import android.app.Application;

public class LocationApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		SDKInitializer.initialize(getApplicationContext());
	}
	
}
