package com.automation.api.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {

	private Properties prop = new Properties();

	/**
	 * Constructor: Read property file from resources folder
	 */
	public  PropertiesReader() {
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
