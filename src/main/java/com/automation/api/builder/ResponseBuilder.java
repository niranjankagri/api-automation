package com.automation.api.builder;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;

import com.automation.api.utils.JsonHelper;
import io.restassured.response.Response;

/**
 * Turns HTTP responses into Java objects.
 * <p>
 * Uses Jackson to map JSON onto the POJOs in
 * {@code com.automation.api.response}. JSON fields that have no matching
 * Java field are ignored, so the API can add fields without breaking the tests.
 */
public class ResponseBuilder {

	// Single shared instance of the builder
	private static ResponseBuilder responseBuilder = new ResponseBuilder();

	/**
	 * Returns the shared instance of this class.
	 *
	 * @return responseBuilder  the shared ResponseBuilder instance.
	 */
	public static ResponseBuilder getResponseBuilder() {
		return responseBuilder;
	}

	/**
	 * Reads the HTTP status code from a response.
	 *
	 * @param response   the response of the HTTP request.
	 * @return int       the status code, e.g. 200.
	 */
	public static int getStatusCode(Response response) {
		return response.getStatusCode();
	}

	/**
	 * Deserializes a single JSON object into an instance of the given class.
	 *
	 * @param responseString   the JSON object as a string.
	 * @param responseClass    the class to map the JSON onto.
	 * @return Object          the mapped object, or null if the JSON could not be parsed.
	 */
	public static Object getResponseAsObject(String responseString, Class<?> responseClass) {
		Object object = null;
		ObjectMapper mapper = new ObjectMapper();
		// Ignore JSON fields that the POJO does not declare
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			object = mapper.readValue(responseString, responseClass);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return object;
	}

	/**
	 * Deserializes a response whose body is a JSON array into a list of objects.
	 *
	 * @param response        the response of the HTTP request; its body must be a JSON array.
	 * @param responseClass   the class to map each array element onto.
	 * @return List&lt;?&gt;  one mapped object per element of the array.
	 */
	public static List<?> getResponseAsObjectList(Response response, Class<?> responseClass) {
		JSONArray array = JsonHelper.toJSONArray(response.asString());
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < array.length(); i++) {
			list.add(getResponseAsObject(array.getJSONObject(i).toString(), responseClass));
		}
		return list;
	}
}
