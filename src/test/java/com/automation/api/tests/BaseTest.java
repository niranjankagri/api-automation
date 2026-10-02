package com.automation.api.tests;

import java.lang.reflect.Method;

import org.testng.annotations.BeforeMethod;

import com.automation.api.manager.CommentManager;
import com.automation.api.manager.PostManager;
import com.automation.api.manager.UserManager;
import com.automation.api.testdata.TestDataLoader;
import com.automation.api.utils.Util;

/**
 * Parent class for all test classes.
 * <p>
 * Gives tests one manager per API resource and the test data, and logs a
 * banner with the test name before each test so the log is easy to follow.
 */
public abstract class BaseTest {

	protected final UserManager userManager = new UserManager();
	protected final PostManager postManager = new PostManager();
	protected final CommentManager commentManager = new CommentManager();
	protected final TestDataLoader testData = TestDataLoader.getInstance();

	/**
	 * Logs the name of the test that is about to run.
	 *
	 * @param method  the test method, injected by TestNG.
	 */
	@BeforeMethod
	public void beforeMethod(Method method) {
		Util.logInfoMessage("################################ " + method.getName() + " ################################");
	}

}
