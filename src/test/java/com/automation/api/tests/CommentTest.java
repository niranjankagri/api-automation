package com.automation.api.tests;

import java.util.List;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.automation.api.response.Comment;
import com.automation.api.response.Post;
import com.automation.api.response.User;

/**
 * Tests for the comments on a user's posts.
 */
public class CommentTest extends BaseTest {

	/**
	 * Checks that every comment on every post of the configured user has a
	 * correctly formatted email address.
	 * <p>
	 * Flow: find the user by username, get their posts, get the comments on
	 * each post, then check each comment's email. All bad emails are reported
	 * together at the end.
	 */
	@Test(description = "Every comment on the configured user's posts has a valid email address")
	public void validateEmailFormatOnComments() {
		SoftAssert softAssert = new SoftAssert();

		// Search for the user.
		User user = userManager.getUser(testData.getUsername());
		// Use the details fetched to make a search for the posts written by the user.
		List<Post> userPosts = postManager.getAllPostsOfUser(user.getId());
		// For each post, fetch the comments.
		List<Comment> commentsOnPosts = commentManager.getAllCommentsOnPosts(userPosts);
		// Validate if the email in the comment section are in the proper format.
		commentManager.validateEmailInAllComments(softAssert, commentsOnPosts);
		softAssert.assertAll();
	}
}
