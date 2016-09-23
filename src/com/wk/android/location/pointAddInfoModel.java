	package com.wk.android.location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.model.LatLng;

public class pointAddInfoModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String info;
	private LatLng point;
	
	
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public LatLng getPoint() {
		return point;
	}
	public void setPoints(LatLng point) {	
		this.point = point;
	}
	
	
	
}
