package com.automation.api.manager;

import com.automation.api.testdata.TestDataLoader;

/**
 * Entry point to the managers, one per API resource (users, posts, comments).
 * <p>
 * Tests extend this class (through {@code BaseTest}) and use the static
 * getters to reach each manager. Because it extends {@link TestDataLoader},
 * test data such as the username is also available to tests.
 */
public class Manager extends TestDataLoader {

	// Shared instance of each manager
	private static UserManager userManager = new UserManager();
	private static CommentManager commentManager = new CommentManager();
	private static PostManager postManager = new PostManager();

	/**
	 * Returns the manager for the {@code /users} endpoint.
	 *
	 * @return userManager  the shared UserManager instance.
	 */
	public static UserManager getUserManager() {
		return userManager;
	}

	/**
	 * Returns the manager for the {@code /comments} endpoint.
	 *
	 * @return commentManager  the shared CommentManager instance.
	 */
	public static CommentManager getCommentManager() {
		return commentManager;
	}

	/**
	 * Returns the manager for the {@code /posts} endpoint.
	 *
	 * @return postManager  the shared PostManager instance.
	 */
	public static PostManager getPostManager() {
		return postManager;
	}

}
