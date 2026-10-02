package com.automation.api.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Reporter;

/**
 * Common helpers: regex matching and logging.
 * <p>
 * Log messages go to the console and to {@code applog.txt}, as configured in
 * {@code src/main/resources/log4j2.properties}.
 */
public class Util {

	// Pattern of a valid email address, e.g. "name@example.com"; matched case-insensitively
	public static String EMAIL_REGEX = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
	final static Logger logger = LogManager.getLogger(Util.class);

	/**
	 * Checks whether a string matches a regex, ignoring case.
	 *
	 * @param  regex    the pattern to match against.
	 * @param  str      the string to check.
	 * @return boolean  true if the pattern is found in the string.
	 */
	public static boolean isStringPresentWithoutCase(String regex, String str) {
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(str);
		return matcher.find();
	}

	/**
	 * Logs a message at INFO level, and records it as a step of the running
	 * test so it appears in the HTML report.
	 *
	 * @param  message  the message to log.
	 */
	public static void logInfoMessage(String message) {
		logger.info(message);
		Reporter.log(message);
	}

}
