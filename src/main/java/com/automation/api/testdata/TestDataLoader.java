package com.automation.api.testdata;

import com.automation.api.utils.PropertiesReader;

public class TestDataLoader extends PropertiesReader {

	private String username;

	public TestDataLoader() {
		applyProperties();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void applyProperties() {
		setUsername(getProp().getProperty("username"));
	}

}
