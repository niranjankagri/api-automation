package com.automation.api.utils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Helpers for parsing JSON strings with the org.json library.
 */
public class JsonHelper {

	/**
	 * Parses a string into a JSONObject.
	 *
	 * @param  jsonString  the JSON object as a string.
	 * @return JSONObject  the parsed object.
	 */
	public static JSONObject toJSONObject(String jsonString) {
		return new JSONObject(jsonString);
	}

	/**
	 * Parses a string into a JSONArray.
	 *
	 * @param  jsonString  the JSON array as a string.
	 * @return JSONArray   the parsed array.
	 */
	public static JSONArray toJSONArray(String jsonString) {
		return new JSONArray(jsonString);
	}

}
