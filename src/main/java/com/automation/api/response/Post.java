package com.automation.api.response;

/**
 * A post returned by the {@code /posts} endpoint.
 * <p>
 * Plain getter/setter POJO that Jackson fills from the JSON response.
 */
public class Post {

	// Unique id of the post
	private int id;
	// Id of the user who wrote the post
	private int userId;
	// Title of the post
	private String title;
	// Text of the post
	private String body;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

}
