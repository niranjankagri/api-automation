package com.automation.api.manager;

import java.util.ArrayList;
import java.util.List;

import org.testng.asserts.SoftAssert;

import com.automation.api.constants.URL;
import com.automation.api.response.Comment;
import com.automation.api.response.Post;
import com.automation.api.utils.Util;

import io.restassured.response.Response;

/**
 * Operations on the {@code /comments} endpoint: fetching the comments of
 * one or more posts and checking the email address on each comment.
 */
public class CommentManager extends Manager {

	/**
	 * Calls the API to get the comments of a post and checks the status code.
	 *
	 * @param postId               the id of the post.
	 * @param expectedStatusCode   the status code the API should return.
	 * @return Response            the response of the API.
	 */
	public Response getAllCommentsOnPostAPI(int postId, int expectedStatusCode) {
		return get(URL.COMMENTS_BY_POSTID + postId, expectedStatusCode);
	}

	/**
	 * Gets all comments on a single post.
	 *
	 * @param postId                the id of the post.
	 * @return List&lt;Comment&gt;  the comments on the post.
	 */
	public List<Comment> getAllCommentsOnPost(int postId) {
		return getList(URL.COMMENTS_BY_POSTID + postId, Comment.class);
	}

	/**
	 * Gets the comments on every post in the list, combined into one list.
	 *
	 * @param posts                 the posts whose comments are fetched.
	 * @return List&lt;Comment&gt;  the comments on all the posts.
	 */
	public List<Comment> getAllCommentsOnPosts(List<Post> posts) {
		List<Comment> comments = new ArrayList<>();
		for (Post post : posts) {
			comments.addAll(getAllCommentsOnPost(post.getId()));
		}
		return comments;
	}

	/**
	 * Checks that the email on every comment is a valid email address
	 * (see {@link Util#isValidEmail(String)}).
	 * <p>
	 * Uses a soft assert so every comment is checked and all bad emails are
	 * reported together; the caller must call {@code softAssert.assertAll()}.
	 *
	 * @param softAssert   the soft assert that collects the results.
	 * @param comments     the comments to check.
	 * @return SoftAssert  the same soft assert, with one result per comment.
	 */
	public SoftAssert validateEmailInAllComments(SoftAssert softAssert, List<Comment> comments) {
		for (Comment comment : comments) {
			boolean valid = Util.isValidEmail(comment.getEmail());
			Util.logInfoMessage("Valid email on comment " + comment.getId() + " : " + comment.getEmail() + " : " + valid);
			softAssert.assertTrue(valid, "Invalid email on comment " + comment.getId() + " : " + comment.getEmail());
		}
		return softAssert;
	}
}
