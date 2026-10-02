package com.automation.api.builder;

import com.automation.api.constants.URL;
import com.automation.api.utils.Util;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Builds and sends HTTP requests to the API under test using REST Assured.
 * <p>
 * Every call to {@link #getRequestBuilder()} resets the base URI to
 * {@link URL#BASE_URL} and starts a fresh request specification, so each
 * request is sent without state left over from a previous one.
 */
public class RequestBuilder {

	// Request specification for the request currently being built
	private static RequestSpecification apiConnection;
	// Single shared instance of the builder
	private static RequestBuilder requestBuilder = new RequestBuilder();

	/**
	 * Prepares a new request against the base URL and returns the shared builder.
	 *
	 * @return requestBuilder  the shared RequestBuilder instance.
	 */
	public static RequestBuilder getRequestBuilder() {
		RestAssured.baseURI = URL.BASE_URL;
		apiConnection = RestAssured.given();
		return requestBuilder;
	}

	/**
	 * Sends a GET request to the given URI, relative to the base URL.
	 *
	 * @param  uri       the endpoint path including any query string, e.g. "/users?username=Bret".
	 * @return response  the response of the HTTP request.
	 */
	public Response get(String uri) {
		Util.logInfoMessage("GET " + URL.BASE_URL + uri);
		return apiConnection.get(uri);
	}

}
