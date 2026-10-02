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

public class UserManager extends Manager {

	/**
     * Call API for search users by username
     * @param  username      the username of user. 
     * @return Response      the response of API.
     */
	public Response searchUserByUsernameAPI(String username, int expectedStatusCode) {
		Response response = RequestBuilder.getRequestBuilder().get(URL.USERS_BY_USERNAME + username);
		Util.logInfoMessage("Actual Status Code : " + response.getStatusCode() + ", Expected Status Code :  : " + expectedStatusCode);
		Assert.assertEquals(response.getStatusCode(), expectedStatusCode, "Response code should be 200");
		return response;
	}
	
	/**
     * Get the detail of users by username
     * @param username      the username of user. 
     * @return List<User>   the list of User class object.
     */
	@SuppressWarnings("unchecked")
	public List<User> getUserByUsername(String username) {
		Response response = searchUserByUsernameAPI(username, StatusCode.OK);
		List<User> usersList =  (List<User>) ResponseBuilder.getResponseAsObjectList(response, User.class);
		Util.logInfoMessage("Username should not be duplicate : " + usersList.size());
		Assert.assertEquals(usersList.size(), 1);
		
		return usersList;
	}
	
	/**
     * Get the detail of user by username
     * @param username   the username of user. 
     * @return User      the User class object.
     */
	public User getUser(String username) {
		return getUserByUsername(username).get(0);
	}
	
}
