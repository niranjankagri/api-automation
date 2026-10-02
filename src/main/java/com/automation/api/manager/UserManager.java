package com.automation.api.manager;

import java.util.List;

import org.testng.Assert;

import com.automation.api.builder.RequestBuilder;
import com.automation.api.builder.ResponseBuilder;
import com.automation.api.constants.StatusCode;
import com.automation.api.constants.URL;
import com.automation.api.response.User;
import com.automation.api.utils.Util;

import io.restassured.response.Response;

/**
 * Operations on the {@code /users} endpoint: searching for a user by username.
 */
public class UserManager extends Manager {

	/**
	 * Calls the API to search users by username and checks the status code.
	 *
	 * @param  username             the username to search for.
	 * @param  expectedStatusCode   the status code the API should return.
	 * @return Response             the response of the API.
	 */
	public Response searchUserByUsernameAPI(String username, int expectedStatusCode) {
		Response response = RequestBuilder.getRequestBuilder().get(URL.USERS_BY_USERNAME + username);
		Util.logInfoMessage("Actual Status Code : " + response.getStatusCode() + ", Expected Status Code :  : " + expectedStatusCode);
		Assert.assertEquals(response.getStatusCode(), expectedStatusCode, "Response code should be 200");
		return response;
	}

	/**
	 * Searches users by username and asserts that exactly one user matches,
	 * since usernames must be unique.
	 *
	 * @param username           the username to search for.
	 * @return List&lt;User&gt;  the matching users (always one element).
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUserByUsername(String username) {
		Response response = searchUserByUsernameAPI(username, StatusCode.OK);
		List<User> usersList = (List<User>) ResponseBuilder.getResponseAsObjectList(response, User.class);
		Util.logInfoMessage("Username should not be duplicate : " + usersList.size());
		Assert.assertEquals(usersList.size(), 1);

		return usersList;
	}

	/**
	 * Gets the single user with the given username.
	 *
	 * @param username   the username to search for.
	 * @return User      the matching user.
	 */
	public User getUser(String username) {
		return getUserByUsername(username).get(0);
	}

}
