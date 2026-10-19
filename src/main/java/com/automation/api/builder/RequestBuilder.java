package com.automation.api.builder;

import com.automation.api.constants.URL;
import com.automation.api.utils.Util;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * Sends HTTP requests to the API under test using REST Assured.
 * <p>
 * Every request is built from a fresh specification with its own base URI,
 * so no state is shared between requests and it is safe to use from
 * parallel tests.
 */
public final class RequestBuilder {

	/** Static helpers only: no instances. */
	private RequestBuilder() {
	}

	/**
	 * Sends a GET request to the given URI, relative to {@link URL#BASE_URL}.
	 *
	 * @param  uri       the endpoint path including any query string, e.g. "/users?username=Bret".
	 * @return response  the response of the HTTP request.
	 */
	public static Response get(String uri) {
		Util.logInfoMessage("GET " + URL.BASE_URL + uri);
		return RestAssured.given().baseUri(URL.BASE_URL).get(uri);
	}

}
