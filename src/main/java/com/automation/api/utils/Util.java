package com.automation.api.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util {

	public static String EMAIL_REGEX = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
	final static Logger logger = LogManager.getLogger(Util.class);
	
	/**
     * Check String is in particular format
     * @param  regex  
     * @param  str
     * @return boolean 
     */
	public static boolean isStringPresentWithoutCase(String regex, String str) {
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(str);
        return matcher.find();
	}
	
	/**
     * Logs a info message
     * @param  message  
     */
	public static void logInfoMessage(String message) {
		logger.info(message);
	}
	
}
