package com.automation.api.manager;

import java.util.List;

import com.automation.api.constants.URL;
import com.automation.api.response.Post;

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
		return get(URL.POSTS_BY_USERID + userId, expectedStatusCode);
	}

	/**
	 * Gets all posts written by a user.
	 *
	 * @param userId             the id of the user.
	 * @return List&lt;Post&gt;  the posts written by the user.
	 */
	public List<Post> getAllPostsOfUser(int userId) {
		return getList(URL.POSTS_BY_USERID + userId, Post.class);
	}

}
