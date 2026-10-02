package com.automation.api.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import com.automation.api.builder.RequestBuilder;
import com.automation.api.builder.ResponseBuilder;
import com.automation.api.constants.StatusCode;
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
		Response response = RequestBuilder.getRequestBuilder().get(URL.COMMENTS_BY_POSTID + postId);
		Util.logInfoMessage("Actual Status Code : " + response.getStatusCode() + ", Expected Status Code :  : " + expectedStatusCode);
		Assert.assertEquals(response.getStatusCode(), expectedStatusCode, "Response code should be 200");
		return response;
	}

	/**
	 * Gets all comments on a single post.
	 *
	 * @param postId                the id of the post.
	 * @return List&lt;Comment&gt;  the comments on the post.
	 */
	@SuppressWarnings("unchecked")
	public List<Comment> getAllCommentsOnPost(int postId) {
		Response response = getAllCommentsOnPostAPI(postId, StatusCode.OK);
		Assert.assertEquals(response.getStatusCode(), StatusCode.OK, "Response code should be 200");

		List<Comment> listOfCommentsOnPost = (List<Comment>) ResponseBuilder.getResponseAsObjectList(response, Comment.class);
		return listOfCommentsOnPost;
	}

	/**
	 * Gets the comments on every post in the list, combined into one list.
	 *
	 * @param userPosts             the posts whose comments are fetched.
	 * @return List&lt;Comment&gt;  the comments on all the posts.
	 */
	public List<Comment> getAllCommentsOnPosts(List<Post> userPosts) {
		List<Comment> listOfCommentsOnPosts = new ArrayList<Comment>();
		for (Iterator<Post> iterator = userPosts.iterator(); iterator.hasNext();) {
			Post post = (Post) iterator.next();
			listOfCommentsOnPosts.addAll(getAllCommentsOnPost(post.getId()));
		}
		return listOfCommentsOnPosts;
	}

	/**
	 * Checks that the email on every comment matches {@link Util#EMAIL_REGEX}.
	 * <p>
	 * Uses a soft assert so every comment is checked and all bad emails are
	 * reported together; the caller must call {@code softAssert.assertAll()}.
	 *
	 * @param softAssert        the soft assert that collects the results.
	 * @param commentsOnPosts   the comments to check.
	 * @return SoftAssert       the same soft assert, with one result per comment.
	 */
	public SoftAssert validateEmailInAllComments(SoftAssert softAssert, List<Comment> commentsOnPosts) {
		boolean validateEmailFormat = false;
		for (Iterator<Comment> iterator = commentsOnPosts.iterator(); iterator.hasNext();) {
			Comment comment = (Comment) iterator.next();
			validateEmailFormat = Util.isStringPresentWithoutCase(Util.EMAIL_REGEX, comment.getEmail());
			Util.logInfoMessage("Is email in correct format : " + comment.getEmail() + " : " + validateEmailFormat);
			softAssert.assertTrue(validateEmailFormat);
		}
		return softAssert;
	}
}
