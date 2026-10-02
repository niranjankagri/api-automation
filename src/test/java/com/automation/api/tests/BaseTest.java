package com.automation.api.tests;

import java.lang.reflect.Method;

import org.testng.annotations.BeforeMethod;

import com.automation.api.manager.Manager;
import com.automation.api.utils.Util;

public class BaseTest extends Manager {

	@BeforeMethod
	public void beforeMethod(Method method) {
		Util.logInfoMessage("################################ " + method.getName() + " ################################" );
	}
	
}
