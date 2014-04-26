package com.parse.residential.parking.models;

import com.google.gson.annotations.SerializedName;

public class Result {

	public static class Keys {
		public static final String GEOMETRY = "geometry";
	}
	
	@SerializedName(Keys.GEOMETRY)
	public Geometry mGeometry;
}