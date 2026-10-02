package com.automation.api.response;

/**
 * A comment returned by the {@code /comments} endpoint.
 * <p>
 * Plain getter/setter POJO that Jackson fills from the JSON response.
 */
public class Comment {

	// Unique id of the comment
	private int id;
	// Id of the post this comment belongs to
	private int postId;
	// Title of the comment
	private String name;
	// Email of the commenter; the tests check its format
	private String email;
	// Text of the comment
	private String body;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPostId() {
		return postId;
	}

	public void setPostId(int postId) {
		this.postId = postId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

}
