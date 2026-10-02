package com.automation.api.utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonHelper {

	/**
     * Convert string to an JsonObject
     * @param  jsonString  the json in string format.
     * @return JSONObject  the JSONObject
     */
	public static JSONObject toJSONObject(String jsonString) {
		return new JSONObject(jsonString);
	}
	
	/**
     * Convert string to an JSONArray
     * @param  jsonString  the json in string format.
     * @return JSONArray  the JSONArray
     */
	public static JSONArray toJSONArray(String jsonString) {
		return new JSONArray(jsonString);
	}
	
}
