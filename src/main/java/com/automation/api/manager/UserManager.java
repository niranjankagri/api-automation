package com.automation.api.manager;

import java.util.List;

import org.testng.Assert;

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
		return get(URL.USERS_BY_USERNAME + username, expectedStatusCode);
	}

	/**
	 * Searches users by username and asserts that exactly one user matches,
	 * since usernames must be unique.
	 *
	 * @param username           the username to search for.
	 * @return List&lt;User&gt;  the matching users (always one element).
	 */
	public List<User> getUserByUsername(String username) {
		List<User> users = getList(URL.USERS_BY_USERNAME + username, User.class);
		Util.logInfoMessage("Users found with username " + username + " : " + users.size());
		Assert.assertEquals(users.size(), 1, "Expected exactly one user with username " + username);
		return users;
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
