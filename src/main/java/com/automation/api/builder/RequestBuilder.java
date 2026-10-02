package com.automation.api.builder;

import com.automation.api.constants.URL;
import com.automation.api.utils.Util;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class RequestBuilder {
	
	private static RequestSpecification apiConnection;
	private static RequestBuilder requestBuilder = new RequestBuilder();
	
	/**
     * Get the instance of class 
     * @return requestBuilder  the class object.
     */
	public static RequestBuilder getRequestBuilder() {
		RestAssured.baseURI = URL.BASE_URL;
		apiConnection = RestAssured.given();
		return requestBuilder;
	}

	/**
     * Constructs a GET Request using provided URI.
     * @param  url       the URL of the request.
     * @return response  the response of HTTP request.
     */
	public Response get(String uri) {
		Util.logInfoMessage("Base url : " + URL.BASE_URL + ", URI : " + uri);
		return apiConnection.get(uri);
	}
	
}
