package com.automation.api.testdata;

import com.automation.api.utils.PropertiesReader;

/**
 * Loads test data from {@code src/resources/data.properties} into typed fields.
 * <p>
 * To add new test data, add a key to the properties file, a field with
 * getter/setter here, and set it in {@link #applyProperties()}.
 */
public class TestDataLoader extends PropertiesReader {

	// Username of the user whose posts and comments are tested
	private String username;

	/**
	 * Reads the properties file (in the parent constructor) and fills the fields.
	 */
	public TestDataLoader() {
		applyProperties();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Copies values from the loaded properties into the fields of this class.
	 */
	public void applyProperties() {
		setUsername(getProp().getProperty("username"));
	}

}
