package com.automation.api.tests;

import java.lang.reflect.Method;

import org.testng.annotations.BeforeMethod;

import com.automation.api.manager.Manager;
import com.automation.api.utils.Util;

/**
 * Parent class for all test classes.
 * <p>
 * Gives tests access to the managers and test data through {@link Manager},
 * and logs a banner with the test name before each test so the log is easy to follow.
 */
public class BaseTest extends Manager {

	/**
	 * Logs the name of the test that is about to run.
	 *
	 * @param method  the test method, injected by TestNG.
	 */
	@BeforeMethod
	public void beforeMethod(Method method) {
		Util.logInfoMessage("################################ " + method.getName() + " ################################" );
	}

}
