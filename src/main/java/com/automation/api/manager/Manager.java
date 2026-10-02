package com.automation.api.manager;

import java.util.List;

import org.testng.Assert;

import com.automation.api.builder.RequestBuilder;
import com.automation.api.builder.ResponseBuilder;
import com.automation.api.constants.StatusCode;
import com.automation.api.utils.Util;

import io.restassured.response.Response;

/**
 * Base class of the managers, one per API resource (users, posts, comments).
 * <p>
 * Holds the steps every manager shares: send the request, log and assert
 * the status code, and map the JSON body onto POJOs.
 */
public abstract class Manager {

	/**
	 * Sends a GET request and asserts the status code.
	 *
	 * @param uri                  the endpoint path including any query string.
	 * @param expectedStatusCode   the status code the API should return.
	 * @return Response            the response of the API.
	 */
	protected Response get(String uri, int expectedStatusCode) {
		Response response = RequestBuilder.get(uri);
		Util.logInfoMessage("Status code : actual " + response.getStatusCode() + ", expected " + expectedStatusCode);
		Assert.assertEquals(response.getStatusCode(), expectedStatusCode, "Unexpected status code for GET " + uri);
		return response;
	}

	/**
	 * Sends a GET request, asserts status 200 and maps the JSON array in the
	 * body onto a list of objects.
	 *
	 * @param uri             the endpoint path including any query string.
	 * @param responseClass   the class to map each array element onto.
	 * @return List&lt;T&gt;  one object per element of the array.
	 */
	protected <T> List<T> getList(String uri, Class<T> responseClass) {
		return ResponseBuilder.getResponseAsObjectList(get(uri, StatusCode.OK), responseClass);
	}

}
