package com.parse.residential.parking.models;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

public class GeoCodeResponse {

	public static class Keys {
		public static final String RESULTS = "results";
	}
	
	@SerializedName(Keys.RESULTS)
	public ArrayList<Result> mResults;
}