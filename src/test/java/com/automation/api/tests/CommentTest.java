package com.automation.api.tests;

import java.util.List;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.automation.api.response.Comment;
import com.automation.api.response.Post;
import com.automation.api.response.User;

public class CommentTest extends BaseTest {

	@Test
	public void validateEmailFormatOnComments() {
		SoftAssert softAssert = new SoftAssert();
		
		// Search for the user.
		User user = getUserManager().getUser(getUsername());
		// Use the details fetched to make a search for the posts written by the user.
		List<Post> userPosts = getPostManager().getAllPostsOfUser(user.getId());
		// For each post, fetch the comments. 
		List<Comment> commentsOnPosts = getCommentManager().getAllCommentsOnPosts(userPosts);
		// Validate if the email in the comment section are in the proper format.
		getCommentManager().validateEmailInAllComments(softAssert, commentsOnPosts);
		softAssert.assertAll();
	}
}
