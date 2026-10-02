package com.automation.api.testdata;

import java.util.Properties;

import com.automation.api.utils.PropertiesReader;

/**
 * Test data, loaded once from {@value #DATA_FILE} on the classpath.
 * <p>
 * Any value can be overridden with a JVM system property of the same name,
 * e.g. {@code mvn test -Dusername=Bret}.
 * <p>
 * To add new test data, add a key to the properties file and a getter here.
 */
public final class TestDataLoader {

	// Properties file with the test data, under src/test/resources
	public static final String DATA_FILE = "data.properties";

	private static final TestDataLoader INSTANCE = new TestDataLoader();

	private final Properties properties;

	private TestDataLoader() {
		properties = PropertiesReader.load(DATA_FILE);
	}

	/**
	 * Returns the shared test data.
	 *
	 * @return TestDataLoader  the shared instance.
	 */
	public static TestDataLoader getInstance() {
		return INSTANCE;
	}

	/**
	 * Username of the user whose posts and comments are tested.
	 *
	 * @return String  the username.
	 */
	public String getUsername() {
		return get("username");
	}

	/**
	 * Returns a value, preferring a system property over the properties file.
	 *
	 * @throws IllegalStateException if the key is set in neither place.
	 */
	private String get(String key) {
		String value = System.getProperty(key, properties.getProperty(key));
		if (value == null || value.isBlank()) {
			throw new IllegalStateException("Missing test data '" + key + "' in " + DATA_FILE);
		}
		return value.trim();
	}

}
