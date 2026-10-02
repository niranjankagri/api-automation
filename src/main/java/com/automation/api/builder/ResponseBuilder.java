package com.automation.api.builder;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;

import com.automation.api.utils.JsonHelper;
import io.restassured.response.Response;

public class ResponseBuilder {
	
	private static ResponseBuilder responseBuilder = new ResponseBuilder();
	
	/**
     * Get the instance of class 
     * @return responseBuilder  the class object.
     */
	public static ResponseBuilder getResponseBuilder() {
		return responseBuilder;
	}

	/**
     * Get status code from HTTP_Response
     * @param response   the response of HTTPRequest
     * @return int       the status code.
     */
    public static int getStatusCode(Response response) {
        return response.getStatusCode();
    }
	
    /**
     * Converting HTTP_Response into respective class object
     * @param responseString   the response of HTTPRequest
     * @param responseClass    the corresponding class
     * @return Object          the class object.
     */
    public static Object getResponseAsObject(String responseString, Class<?> responseClass) {
        Object object = null;
    	ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
        	object = mapper.readValue(responseString, responseClass);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
        return object;
    }
    
    /**
     * Converting HTTP_Response into respective class object
     * @param response   the response of HTTPRequest
     * @param responseClass    the corresponding class
     * @return List<?>          the list of object.
     */
    public static List<?> getResponseAsObjectList(Response response, Class<?> responseClass) {
    	JSONArray array = JsonHelper.toJSONArray(response.asString());
    	List<Object> list =  new ArrayList<Object>();
		for (int i = 0; i < array.length(); i++) {
			list.add(getResponseAsObject(array.getJSONObject(i).toString(), responseClass));
		}
		return list;
    }
}
