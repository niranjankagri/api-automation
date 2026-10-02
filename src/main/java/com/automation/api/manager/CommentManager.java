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

public class CommentManager extends Manager {

	/**
     * Call API for get comments by postid
     * @param postId           the id of a Post
     * @return Response      the response of API.
     */
	public Response getAllCommentsOnPostAPI(int postId, int expectedStatusCode) {
		Response response = RequestBuilder.getRequestBuilder().get(URL.COMMENTS_BY_POSTID + postId);
		Util.logInfoMessage("Actual Status Code : " + response.getStatusCode() + ", Expected Status Code :  : " + expectedStatusCode);
		Assert.assertEquals(response.getStatusCode(), expectedStatusCode, "Response code should be 200");
		return response;
	}
	
	/**
     * Get the list of all comments done on a particular posts
     * @param postId           the id of a Post
     * @return List<Comment>   the list of Comment class object
     */
	@SuppressWarnings("unchecked")
	public List<Comment> getAllCommentsOnPost(int postId) {
		Response response = getAllCommentsOnPostAPI(postId, StatusCode.OK);
		Assert.assertEquals(response.getStatusCode(), StatusCode.OK, "Response code should be 200"); 
		
		List<Comment> listOfCommentsOnPost =  (List<Comment>) ResponseBuilder.getResponseAsObjectList(response, Comment.class);
		return listOfCommentsOnPost;
	}
	
	/**
     * Get the list of all comments done on posts
     * @param List<Post>   the list of Post class object
     * @return List<Comment>   the list of Comment class object
     */
	public List<Comment> getAllCommentsOnPosts(List<Post> userPosts) {
		List<Comment> listOfCommentsOnPosts =  new ArrayList<Comment>();
		for (Iterator<Post> iterator = userPosts.iterator(); iterator.hasNext();) {
			Post post = (Post) iterator.next();
			listOfCommentsOnPosts.addAll(getAllCommentsOnPost(post.getId()));
		}
		return listOfCommentsOnPosts;
	}
	
	/**
     * Validate email format on all comments 
     * @param   List<Comment>  the list of Comment class object
     * @return  SoftAssert     the assert value of all emails.
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
