package com.automation.api.manager;

import java.util.List;
import org.testng.Assert;
import com.automation.api.builder.RequestBuilder;
import com.automation.api.builder.ResponseBuilder;
import com.automation.api.constants.StatusCode;
import com.automation.api.constants.URL;
import com.automation.api.response.Post;
import com.automation.api.utils.Util;

import io.restassured.response.Response;

public class PostManager extends Manager {
	
	/**
     * Call API for get posts by userid
     * @param  userId        the user ID of user. 
     * @return Response      the response of API.
     */
	public Response getAllPostsOfUserAPI(int userId, int expectedStatusCode) {
		Response response = RequestBuilder.getRequestBuilder().get(URL.POSTS_BY_USERID + userId);
		Util.logInfoMessage("Actual Status Code : " + response.getStatusCode() + ", Expected Status Code :  : " + expectedStatusCode);
		Assert.assertEquals(response.getStatusCode(), expectedStatusCode, "Response code should be 200");
		return response;
	}
	
	/**
     * Get the list of all post done by user 
     * @param userId        the user ID of user
     * @return List<Post>   the list of Post class object
     */
	@SuppressWarnings("unchecked")
	public List<Post> getAllPostsOfUser(int userId) {
		Response response = getAllPostsOfUserAPI(userId, StatusCode.OK);
		List<Post> userPostsList =  (List<Post>) ResponseBuilder.getResponseAsObjectList(response, Post.class);
		return userPostsList;
	}
	
}
