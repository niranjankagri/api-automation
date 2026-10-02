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

/**
 * Operations on the {@code /posts} endpoint: fetching the posts written by a user.
 */
public class PostManager extends Manager {

	/**
	 * Calls the API to get the posts of a user and checks the status code.
	 *
	 * @param  userId               the id of the user.
	 * @param  expectedStatusCode   the status code the API should return.
	 * @return Response             the response of the API.
	 */
	public Response getAllPostsOfUserAPI(int userId, int expectedStatusCode) {
		Response response = RequestBuilder.getRequestBuilder().get(URL.POSTS_BY_USERID + userId);
		Util.logInfoMessage("Actual Status Code : " + response.getStatusCode() + ", Expected Status Code :  : " + expectedStatusCode);
		Assert.assertEquals(response.getStatusCode(), expectedStatusCode, "Response code should be 200");
		return response;
	}

	/**
	 * Gets all posts written by a user.
	 *
	 * @param userId             the id of the user.
	 * @return List&lt;Post&gt;  the posts written by the user.
	 */
	@SuppressWarnings("unchecked")
	public List<Post> getAllPostsOfUser(int userId) {
		Response response = getAllPostsOfUserAPI(userId, StatusCode.OK);
		List<Post> userPostsList = (List<Post>) ResponseBuilder.getResponseAsObjectList(response, Post.class);
		return userPostsList;
	}

}
