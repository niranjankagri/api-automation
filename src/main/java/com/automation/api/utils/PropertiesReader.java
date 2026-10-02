package com.automation.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Loads {@code .properties} files from the classpath
 * ({@code src/main/resources} or {@code src/test/resources}), so it works
 * no matter which directory the tests are started from.
 */
public final class PropertiesReader {

	private PropertiesReader() {
	}

	/**
	 * Loads a properties file from the classpath.
	 *
	 * @param resource      the file name relative to the resources folder, e.g. "data.properties".
	 * @return Properties   the key/value pairs in the file.
	 * @throws IllegalStateException if the file is not on the classpath.
	 * @throws UncheckedIOException  if the file cannot be read.
	 */
	public static Properties load(String resource) {
		Properties properties = new Properties();
		try (InputStream inputStream = PropertiesReader.class.getClassLoader().getResourceAsStream(resource)) {
			if (inputStream == null) {
				throw new IllegalStateException("Properties file not found on classpath : " + resource);
			}
			properties.load(inputStream);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not read properties file : " + resource, e);
		}
		return properties;
	}

}
