package com.automation.api.constants;

/**
 * Base URL and endpoint paths of the API under test
 * (<a href="https://jsonplaceholder.typicode.com">JSONPlaceholder</a>).
 * <p>
 * Each endpoint ends with a query parameter, so callers append the value,
 * e.g. {@code URL.POSTS_BY_USERID + 1}.
 */
public final class URL {

	// Root URL of the API; endpoints below are relative to it
	public static final String BASE_URL = "https://jsonplaceholder.typicode.com";
	// Endpoint to search users by username
	public static final String USERS_BY_USERNAME = "/users?username=";
	// Endpoint to get all posts written by a user
	public static final String POSTS_BY_USERID = "/posts?userId=";
	// Endpoint to get all comments of a post
	public static final String COMMENTS_BY_POSTID = "/comments?postId=";

	/** Constants only: no instances. */
	private URL() {
	}

}
