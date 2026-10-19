package com.automation.api.utils;

import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Reporter;

/**
 * Common helpers: email validation and logging.
 * <p>
 * Log messages go to the console and to {@code applog.txt}, as configured in
 * {@code src/main/resources/log4j2.properties}.
 */
public final class Util {

	// Pattern of a valid email address, e.g. "name@example.com"; matched case-insensitively
	public static final String EMAIL_REGEX = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
	private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
	private static final Logger logger = LogManager.getLogger(Util.class);

	/** Static helpers only: no instances. */
	private Util() {
	}

	/**
	 * Checks whether a string is a valid email address ({@link #EMAIL_REGEX}).
	 *
	 * @param  email    the string to check; may be null.
	 * @return boolean  true if the string is a valid email address.
	 */
	public static boolean isValidEmail(String email) {
		return email != null && EMAIL_PATTERN.matcher(email).matches();
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
