package com.parse.residential.parking.models;

import com.google.gson.annotations.SerializedName;

public class Geometry {
	
	public static class Keys {
		public static final String LOCATION = "location";
	}
	
	@SerializedName(Keys.LOCATION)
	public Location mLocation;
	
}
