package com.parse.residential.parking.models;

import com.google.gson.annotations.SerializedName;

public class Location {

	public static class Keys {
		public static final String LAT = "lat";
		public static final String LNG = "lng";
	}

	@SerializedName(Keys.LAT)
	public Float mLatitude;

	@SerializedName(Keys.LNG)
	public Float mLongitude;

}
