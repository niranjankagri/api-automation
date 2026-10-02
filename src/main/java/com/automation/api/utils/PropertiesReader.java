package com.automation.api.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads the test data file {@code src/resources/data.properties}.
 * <p>
 * The path is resolved from the working directory, so tests must be run
 * from the project root (as Maven and the IDE do by default).
 */
public class PropertiesReader {

	// Key/value pairs loaded from the properties file
	private Properties prop = new Properties();

	/**
	 * Loads the properties file from the resources folder. If the file cannot
	 * be read, the error is printed and the properties stay empty.
	 */
	public PropertiesReader() {
		try {
			InputStream inputStream = new FileInputStream(System.getProperty("user.dir") + "/src/resources/data.properties");
			prop.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Properties getProp() {
		return prop;
	}

	public void setProp(Properties prop) {
		this.prop = prop;
	}

}
