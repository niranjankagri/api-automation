package com.automation.api.listeners;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.xml.XmlSuite;

import com.automation.api.utils.Util;

/**
 * TestNG reporter that writes a single, self-contained HTML report
 * ({@value #REPORT_FILE_NAME}) after the whole suite has run.
 * <p>
 * The report shows a summary (status, pass-rate ring, counts, duration) and
 * one expandable card per test with its status, duration, the steps logged
 * through {@link Util#logInfoMessage(String)} and, for failures, the error
 * and stack trace. Known step formats are rendered specially:
 * <ul>
 * <li>{@code GET https://...} as a request with an HTTP method badge,</li>
 * <li>{@code Status code : actual 200, expected 200} as a status chip,</li>
 * <li>{@code <text> : true|false} as a passed / failed check.</li>
 * </ul>
 * Any other message is shown as a plain info step. All CSS and JavaScript
 * are inline, so the file can be opened or shared on its own.
 * <p>
 * Register it in {@code testng.xml} under {@code <listeners>}. The file is
 * written to TestNG's output directory: {@code target/surefire-reports} when
 * run with Maven, {@code test-output} when run from the IDE.
 */
public class HtmlReportListener implements IReporter {

	// Name of the generated report file
	public static final String REPORT_FILE_NAME = "api-test-report.html";

	private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss")
			.withZone(ZoneId.systemDefault());
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss")
			.withZone(ZoneId.systemDefault());

	// Step formats that get special rendering (see class comment)
	private static final Pattern REQUEST_STEP = Pattern.compile("^(GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS) (\\S+)$");
	private static final Pattern STATUS_STEP = Pattern.compile("^Status code : actual (\\d+), expected (\\d+)$");
	private static final Pattern CHECK_STEP = Pattern.compile("^(.+?) : (true|false)$");
	// Splits a URL into host, path and query
	private static final Pattern URL_PARTS = Pattern.compile("^(https?://[^/]+)?([^?]*)(\\?.*)?$");

	// Circumference of the pass-rate ring (SVG circle with radius 52)
	private static final double RING_CIRCUMFERENCE = 2 * Math.PI * 52;

	/**
	 * Called by TestNG once all suites have finished. Collects every test
	 * result and writes the HTML report.
	 *
	 * @param xmlSuites         the suites as defined in testng.xml.
	 * @param suites            the suites that ran, with their results.
	 * @param outputDirectory   TestNG's output directory, where the report is written.
	 */
	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		List<ITestResult> results = new ArrayList<>();
		for (ISuite suite : suites) {
			for (ISuiteResult suiteResult : suite.getResults().values()) {
				ITestContext context = suiteResult.getTestContext();
				results.addAll(context.getPassedTests().getAllResults());
				results.addAll(context.getFailedTests().getAllResults());
				results.addAll(context.getSkippedTests().getAllResults());
			}
		}
		results.sort(Comparator.comparingLong(ITestResult::getStartMillis));

		String suiteName = suites.isEmpty() ? "Test Suite" : suites.get(0).getName();
		Path reportPath = Paths.get(outputDirectory, REPORT_FILE_NAME);
		try {
			Files.createDirectories(reportPath.getParent());
			Files.writeString(reportPath, buildHtml(suiteName, results), StandardCharsets.UTF_8);
			Util.logInfoMessage("HTML report generated : " + reportPath.toAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Builds the full HTML page.
	 *
	 * @param suiteName   the name shown in the report header.
	 * @param results     every test result, in execution order.
	 * @return String     the HTML document.
	 */
	private String buildHtml(String suiteName, List<ITestResult> results) {
		int passed = count(results, ITestResult.SUCCESS);
		int failed = count(results, ITestResult.FAILURE);
		int skipped = count(results, ITestResult.SKIP);
		int total = results.size();

		long start = results.stream().mapToLong(ITestResult::getStartMillis).min().orElse(System.currentTimeMillis());
		long end = results.stream().mapToLong(ITestResult::getEndMillis).max().orElse(start);
		long longestTest = results.stream().mapToLong(r -> r.getEndMillis() - r.getStartMillis()).max().orElse(1);
		int requests = 0;
		int checks = 0;

		StringBuilder rows = new StringBuilder();
		int index = 0;
		for (ITestResult result : results) {
			List<String> steps = Reporter.getOutput(result);
			requests += (int) steps.stream().filter(s -> REQUEST_STEP.matcher(s).matches()).count();
			checks += (int) steps.stream().filter(s -> CHECK_STEP.matcher(s).matches()).count();
			rows.append(buildTestCard(result, steps, ++index, Math.max(longestTest, 1)));
		}
		if (results.isEmpty()) {
			rows.append("<div class=\"empty\">No tests were run.</div>");
		}

		String status = failed > 0 ? "fail" : (total == 0 || passed == 0 ? "skip" : "pass");
		String statusText = failed > 0 ? failed + (failed == 1 ? " test failed" : " tests failed")
				: total == 0 ? "No tests run" : passed == 0 ? "All tests skipped" : "All tests passed";

		return TEMPLATE
				.replace("{{SUITE}}", escape(suiteName))
				.replace("{{STATUS}}", status)
				.replace("{{STATUS_TEXT}}", statusText)
				.replace("{{STARTED}}", DATE_TIME.format(Instant.ofEpochMilli(start)))
				.replace("{{DURATION}}", formatDuration(end - start))
				.replace("{{TOTAL}}", String.valueOf(total))
				.replace("{{PASSED}}", String.valueOf(passed))
				.replace("{{FAILED}}", String.valueOf(failed))
				.replace("{{SKIPPED}}", String.valueOf(skipped))
				.replace("{{REQUESTS}}", String.valueOf(requests))
				.replace("{{CHECKS}}", String.valueOf(checks))
				.replace("{{PASS_RATE}}", total == 0 ? "–" : String.format(Locale.ROOT, "%.0f", passed * 100.0 / total))
				.replace("{{RING}}", buildRing(passed, failed, skipped, total))
				.replace("{{BAR_PASS}}", percent(passed, total))
				.replace("{{BAR_FAIL}}", percent(failed, total))
				.replace("{{BAR_SKIP}}", percent(skipped, total))
				.replace("{{JAVA}}", escape(System.getProperty("java.version")))
				.replace("{{OS}}", escape(System.getProperty("os.name")))
				.replace("{{USER}}", escape(System.getProperty("user.name")))
				.replace("{{ROWS}}", rows.toString());
	}

	/**
	 * Builds the SVG segments of the pass-rate ring: one arc per status,
	 * drawn one after another around the circle.
	 */
	private String buildRing(int passed, int failed, int skipped, int total) {
		if (total == 0) {
			return "";
		}
		StringBuilder svg = new StringBuilder();
		double offset = 0;
		int[] counts = { passed, failed, skipped };
		String[] colors = { "var(--pass)", "var(--fail)", "var(--skip)" };
		for (int i = 0; i < counts.length; i++) {
			if (counts[i] == 0) {
				continue;
			}
			double length = RING_CIRCUMFERENCE * counts[i] / total;
			svg.append(String.format(Locale.ROOT,
					"<circle class=\"seg\" cx=\"60\" cy=\"60\" r=\"52\" stroke=\"%s\" stroke-dasharray=\"%.2f %.2f\" stroke-dashoffset=\"%.2f\"/>",
					colors[i], length, RING_CIRCUMFERENCE - length, -offset));
			offset += length;
		}
		return svg.toString();
	}

	/**
	 * Builds the expandable card for one test: header line plus details
	 * (meta data, logged steps, error and stack trace).
	 *
	 * @param result        the result of the test.
	 * @param steps         the messages logged during the test.
	 * @param index         the 1-based position of the test in the report.
	 * @param longestTest   duration of the slowest test, used to scale the duration bar.
	 * @return String       the HTML for the test.
	 */
	private String buildTestCard(ITestResult result, List<String> steps, int index, long longestTest) {
		String status = statusName(result.getStatus());
		String description = result.getMethod().getDescription();
		long duration = result.getEndMillis() - result.getStartMillis();
		Throwable error = result.getThrowable();
		String className = result.getTestClass().getRealClass().getSimpleName();

		int requests = 0;
		int checksPassed = 0;
		int checksFailed = 0;
		StringBuilder stepList = new StringBuilder();
		for (String step : steps) {
			if (REQUEST_STEP.matcher(step).matches()) {
				requests++;
			}
			Matcher check = CHECK_STEP.matcher(step);
			if (check.matches()) {
				if (Boolean.parseBoolean(check.group(2))) {
					checksPassed++;
				} else {
					checksFailed++;
				}
			}
			stepList.append(renderStep(step));
		}

		StringBuilder body = new StringBuilder();
		if (description != null && !description.isBlank()) {
			body.append("<p class=\"desc\">").append(escape(description)).append("</p>");
		}
		body.append("<div class=\"meta\">")
				.append(metaItem("Class", result.getTestClass().getName()))
				.append(metaItem("Started", TIME.format(Instant.ofEpochMilli(result.getStartMillis()))))
				.append(metaItem("Duration", formatDuration(duration)))
				.append(metaItem("Status", "pass".equals(status) ? "Passed" : "fail".equals(status) ? "Failed" : "Skipped"))
				.append("</div>");

		if (error != null) {
			body.append("<div class=\"error-box ").append(status).append("\">")
					.append("<div class=\"error-title\">").append("skip".equals(status) ? "Skip reason" : escape(error.getClass().getSimpleName())).append("</div>")
					.append("<pre>").append(escape(String.valueOf(error.getMessage()))).append("</pre>")
					.append("<details class=\"trace\"><summary>Show stack trace</summary><pre>").append(escape(stackTrace(error)))
					.append("</pre></details></div>");
		}

		if (!steps.isEmpty()) {
			body.append("<div class=\"steps-head\"><h4>Steps <span>").append(steps.size()).append("</span></h4>")
					.append("<div class=\"chips\">")
					.append(requests > 0 ? "<span class=\"chip\">" + plural(requests, "request") + "</span>" : "")
					.append(checksPassed > 0 ? "<span class=\"chip ok\">✓ " + plural(checksPassed, "check") + " passed</span>" : "")
					.append(checksFailed > 0 ? "<span class=\"chip bad\">✗ " + plural(checksFailed, "check") + " failed</span>" : "")
					.append("</div></div>")
					.append("<ol class=\"steps\">").append(stepList).append("</ol>");
		}

		String barWidth = String.format(Locale.ROOT, "%.1f", Math.max(duration * 100.0 / longestTest, 2));
		return "<details class=\"test " + status + "\" data-status=\"" + status + "\" data-name=\""
				+ escape((result.getMethod().getMethodName() + " " + className + " " + (description == null ? "" : description)).toLowerCase(Locale.ROOT)) + "\""
				+ ("fail".equals(status) ? " open" : "") + " style=\"--i:" + index + "\">"
				+ "<summary>"
				+ "<span class=\"status-icon " + status + "\">" + statusIcon(status) + "</span>"
				+ "<span class=\"title\"><b>" + escape(humanize(result.getMethod().getMethodName())) + "</b>"
				+ "<small>" + escape(className) + " · " + escape(result.getMethod().getMethodName()) + "</small></span>"
				+ "<span class=\"dur\"><span class=\"dur-text\">" + formatDuration(duration) + "</span>"
				+ "<span class=\"dur-bar\"><i style=\"width:" + barWidth + "%\"></i></span></span>"
				+ "<span class=\"chev\" aria-hidden=\"true\"></span>"
				+ "</summary>"
				+ "<div class=\"body\">" + body + "</div>"
				+ "</details>";
	}

	/**
	 * Renders one logged step, using a special layout for requests,
	 * status codes and true/false checks.
	 */
	private String renderStep(String step) {
		Matcher m = REQUEST_STEP.matcher(step);
		if (m.matches()) {
			return "<li class=\"step req\"><span class=\"method " + m.group(1).toLowerCase(Locale.ROOT) + "\">" + m.group(1)
					+ "</span><code>" + highlightUrl(m.group(2)) + "</code></li>";
		}
		m = STATUS_STEP.matcher(step);
		if (m.matches()) {
			boolean ok = m.group(1).equals(m.group(2));
			return "<li class=\"step status " + (ok ? "ok" : "bad") + "\"><span class=\"code\">" + m.group(1) + "</span>"
					+ (ok ? "Status code as expected" : "Expected status " + m.group(2)) + "</li>";
		}
		m = CHECK_STEP.matcher(step);
		if (m.matches()) {
			boolean ok = Boolean.parseBoolean(m.group(2));
			return "<li class=\"step check " + (ok ? "ok" : "bad") + "\"><span class=\"tick\">" + (ok ? "✓" : "✗") + "</span>"
					+ escape(m.group(1)) + "</li>";
		}
		return "<li class=\"step info\"><span class=\"dot\"></span>" + escape(step) + "</li>";
	}

	/**
	 * Splits a URL into host, path and query parts so they can be coloured separately.
	 */
	private String highlightUrl(String url) {
		Matcher m = URL_PARTS.matcher(url);
		if (!m.matches()) {
			return escape(url);
		}
		return "<span class=\"host\">" + escape(m.group(1) == null ? "" : m.group(1)) + "</span>"
				+ "<span class=\"path\">" + escape(m.group(2)) + "</span>"
				+ "<span class=\"query\">" + escape(m.group(3) == null ? "" : m.group(3)) + "</span>";
	}

	/**
	 * Turns a camelCase method name into a readable title,
	 * e.g. "validateEmailFormatOnComments" into "Validate email format on comments".
	 */
	private String humanize(String methodName) {
		String words = methodName.replaceAll("([a-z0-9])([A-Z])", "$1 $2").toLowerCase(Locale.ROOT);
		return words.isEmpty() ? methodName : Character.toUpperCase(words.charAt(0)) + words.substring(1);
	}

	/**
	 * Returns e.g. "1 request" or "12 requests".
	 */
	private String plural(int count, String word) {
		return count + " " + word + (count == 1 ? "" : "s");
	}

	private String metaItem(String label, String value) {
		return "<div><span>" + label + "</span><b>" + escape(value) + "</b></div>";
	}

	private String statusIcon(String status) {
		switch (status) {
		case "pass":
			return "✓";
		case "fail":
			return "✗";
		default:
			return "–";
		}
	}

	/**
	 * Counts the results with the given TestNG status.
	 */
	private int count(List<ITestResult> results, int status) {
		return (int) results.stream().filter(r -> r.getStatus() == status).count();
	}

	/**
	 * Returns part / total as a CSS percentage value, e.g. "66.67".
	 */
	private String percent(int part, int total) {
		return total == 0 ? "0" : String.format(Locale.ROOT, "%.2f", part * 100.0 / total);
	}

	/**
	 * Maps a TestNG status code to the CSS class / filter name used in the report.
	 */
	private String statusName(int status) {
		switch (status) {
		case ITestResult.SUCCESS:
			return "pass";
		case ITestResult.FAILURE:
			return "fail";
		default:
			return "skip";
		}
	}

	/**
	 * Formats milliseconds as e.g. "850 ms", "4.21 s" or "2m 05s".
	 */
	private String formatDuration(long millis) {
		if (millis < 1000) {
			return millis + " ms";
		}
		if (millis < 60_000) {
			return String.format(Locale.ROOT, "%.2f s", millis / 1000.0);
		}
		return String.format(Locale.ROOT, "%dm %02ds", millis / 60_000, (millis / 1000) % 60);
	}

	/**
	 * Returns the full stack trace of an exception as a string.
	 */
	private String stackTrace(Throwable error) {
		StringWriter writer = new StringWriter();
		error.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}

	/**
	 * Escapes text for safe use inside HTML.
	 */
	private String escape(String text) {
		if (text == null) {
			return "";
		}
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

	// HTML page with {{PLACEHOLDERS}} filled in by buildHtml()
	private static final String TEMPLATE = """
			<!doctype html>
			<html lang="en">
			<head>
			<meta charset="utf-8">
			<meta name="viewport" content="width=device-width, initial-scale=1">
			<title>{{SUITE}} · Test Report</title>
			<link rel="preconnect" href="https://fonts.googleapis.com">
			<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
			<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
			<style>
			  :root {
			    --bg: #f4f5fa; --surface: #ffffff; --surface-2: #f7f8fc; --text: #141a2e; --muted: #6a7290; --faint: #9aa1b9;
			    --border: #e6e8f0; --shadow: 0 1px 2px rgba(20,26,46,.04), 0 8px 24px -12px rgba(20,26,46,.12);
			    --pass: #12b76a; --pass-soft: #e7f8ef; --fail: #f04438; --fail-soft: #feeceb; --skip: #f79009; --skip-soft: #fef4e6;
			    --accent: #5b5bf6; --accent-soft: #eeeefe; --code: #f3f4f9;
			    --hero-1: #1b1f4b; --hero-2: #3b2a8c; --hero-3: #5b5bf6;
			    color-scheme: light;
			  }
			  :root[data-theme="dark"] {
			    --bg: #0b0d17; --surface: #141726; --surface-2: #191d2f; --text: #e8eaf4; --muted: #9097b3; --faint: #636a87;
			    --border: #252a40; --shadow: 0 1px 2px rgba(0,0,0,.3), 0 12px 32px -16px rgba(0,0,0,.6);
			    --pass: #32d583; --pass-soft: #12301f; --fail: #ff6b5f; --fail-soft: #3a1817; --skip: #fdb022; --skip-soft: #382810;
			    --accent: #8b8bff; --accent-soft: #23244a; --code: #0f1220;
			    --hero-1: #0f1130; --hero-2: #2a1f6a; --hero-3: #4a4ad8;
			    color-scheme: dark;
			  }
			  @media (prefers-color-scheme: dark) {
			    :root:not([data-theme="light"]) {
			      --bg: #0b0d17; --surface: #141726; --surface-2: #191d2f; --text: #e8eaf4; --muted: #9097b3; --faint: #636a87;
			      --border: #252a40; --shadow: 0 1px 2px rgba(0,0,0,.3), 0 12px 32px -16px rgba(0,0,0,.6);
			      --pass: #32d583; --pass-soft: #12301f; --fail: #ff6b5f; --fail-soft: #3a1817; --skip: #fdb022; --skip-soft: #382810;
			      --accent: #8b8bff; --accent-soft: #23244a; --code: #0f1220;
			      --hero-1: #0f1130; --hero-2: #2a1f6a; --hero-3: #4a4ad8;
			      color-scheme: dark;
			    }
			  }
			  * { box-sizing: border-box; }
			  html { -webkit-text-size-adjust: 100%; }
			  body { margin: 0; background: var(--bg); color: var(--text);
			         font: 14.5px/1.55 Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
			         -webkit-font-smoothing: antialiased; }
			  code, pre, .mono { font-family: "JetBrains Mono", ui-monospace, SFMono-Regular, Consolas, monospace; }
			  .wrap { max-width: 1120px; margin: 0 auto; padding: 0 16px; }

			  /* ---------- Hero ---------- */
			  .hero { position: relative; overflow: hidden; color: #fff; padding: 36px 0 112px;
			          background: radial-gradient(1200px 400px at 85% -20%, var(--hero-3), transparent 60%),
			                      linear-gradient(135deg, var(--hero-1), var(--hero-2)); }
			  .hero::after { content: ""; position: absolute; inset: 0; opacity: .12; pointer-events: none;
			                 background-image: radial-gradient(rgba(255,255,255,.9) 1px, transparent 1px); background-size: 22px 22px;
			                 mask-image: linear-gradient(to bottom, #000, transparent 85%); -webkit-mask-image: linear-gradient(to bottom, #000, transparent 85%); }
			  .topbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 28px; position: relative; z-index: 1; }
			  .brand { display: flex; align-items: center; gap: 10px; font-weight: 600; font-size: 13px; letter-spacing: .02em; opacity: .9; }
			  .logo { width: 30px; height: 30px; border-radius: 9px; display: grid; place-items: center;
			          background: rgba(255,255,255,.14); border: 1px solid rgba(255,255,255,.22); }
			  .icon-btn { width: 36px; height: 36px; border-radius: 10px; border: 1px solid rgba(255,255,255,.22); cursor: pointer;
			              background: rgba(255,255,255,.1); color: #fff; display: grid; place-items: center; transition: background .2s; }
			  .icon-btn:hover { background: rgba(255,255,255,.2); }
			  .hero-main { display: flex; flex-wrap: wrap; align-items: flex-end; justify-content: space-between; gap: 16px; position: relative; z-index: 1; }
			  .eyebrow { text-transform: uppercase; letter-spacing: .14em; font-size: 11.5px; font-weight: 600; opacity: .7; margin-bottom: 6px; }
			  h1 { margin: 0; font-size: clamp(26px, 4vw, 38px); font-weight: 800; letter-spacing: -0.03em; line-height: 1.1; }
			  .hero-meta { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 14px; }
			  .hero-meta span { font-size: 12.5px; padding: 5px 11px; border-radius: 999px; background: rgba(255,255,255,.1);
			                    border: 1px solid rgba(255,255,255,.16); white-space: nowrap; }
			  .hero-meta b { font-weight: 600; }
			  .verdict { display: inline-flex; align-items: center; gap: 10px; padding: 10px 18px 10px 12px; border-radius: 999px;
			             font-weight: 700; font-size: 14px; background: rgba(255,255,255,.95); color: #141a2e; box-shadow: 0 10px 30px -10px rgba(0,0,0,.5); }
			  .verdict i { width: 26px; height: 26px; border-radius: 50%; display: grid; place-items: center; color: #fff; font-style: normal; font-size: 14px; }
			  .verdict.pass i { background: #12b76a; } .verdict.fail i { background: #f04438; } .verdict.skip i { background: #f79009; }
			  .verdict.pass i::before { content: "✓"; } .verdict.fail i::before { content: "✗"; } .verdict.skip i::before { content: "–"; }

			  /* ---------- Summary ---------- */
			  .summary { margin-top: -84px; position: relative; z-index: 2; display: grid; grid-template-columns: 300px 1fr; gap: 16px; }
			  .card { background: var(--surface); border: 1px solid var(--border); border-radius: 18px; box-shadow: var(--shadow); }
			  .ring-card { padding: 24px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16px; }
			  .ring { position: relative; width: 168px; height: 168px; }
			  .ring svg { width: 100%; height: 100%; transform: rotate(-90deg); }
			  .ring .track { fill: none; stroke: var(--code); stroke-width: 12; }
			  .ring .seg { fill: none; stroke-width: 12; stroke-linecap: butt; animation: draw 1s cubic-bezier(.2,.8,.2,1) both; }
			  @keyframes draw { from { stroke-dasharray: 0 400; } }
			  .ring-label { position: absolute; inset: 0; display: grid; place-items: center; text-align: center; }
			  .ring-label b { display: block; font-size: 40px; font-weight: 800; letter-spacing: -0.04em; line-height: 1; }
			  .ring-label b small { font-size: 18px; font-weight: 700; color: var(--muted); }
			  .ring-label span { font-size: 12px; color: var(--muted); text-transform: uppercase; letter-spacing: .1em; font-weight: 600; }
			  .legend { display: flex; gap: 16px; font-size: 12.5px; color: var(--muted); }
			  .legend span { display: inline-flex; align-items: center; gap: 6px; }
			  .legend i { width: 9px; height: 9px; border-radius: 3px; }

			  .stats-card { padding: 22px; display: flex; flex-direction: column; gap: 18px; }
			  .stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
			  .stat { position: relative; border-radius: 14px; padding: 16px; background: var(--surface-2); border: 1px solid var(--border); overflow: hidden; }
			  .stat::before { content: ""; position: absolute; left: 0; top: 0; bottom: 0; width: 3px; background: var(--muted); }
			  .stat.pass::before { background: var(--pass); } .stat.fail::before { background: var(--fail); } .stat.skip::before { background: var(--skip); }
			  .stat.total::before { background: var(--accent); }
			  .stat .label { font-size: 11.5px; text-transform: uppercase; letter-spacing: .09em; color: var(--muted); font-weight: 600; }
			  .stat .value { display: block; font-size: 32px; font-weight: 800; letter-spacing: -0.03em; line-height: 1.15; margin-top: 4px; font-variant-numeric: tabular-nums; }
			  .stat.pass .value { color: var(--pass); } .stat.fail .value { color: var(--fail); } .stat.skip .value { color: var(--skip); }
			  .bar { display: flex; height: 10px; border-radius: 999px; overflow: hidden; background: var(--code); }
			  .bar i { display: block; height: 100%; transition: width .8s; }
			  .bar .p { background: var(--pass); } .bar .f { background: var(--fail); } .bar .s { background: var(--skip); }
			  .facts { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
			  .fact { display: flex; flex-direction: column; gap: 2px; }
			  .fact span { font-size: 11.5px; color: var(--muted); text-transform: uppercase; letter-spacing: .08em; font-weight: 600; }
			  .fact b { font-size: 15px; font-weight: 600; overflow-wrap: anywhere; }

			  /* ---------- Toolbar ---------- */
			  .toolbar { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; margin: 36px 0 14px; }
			  .toolbar h2 { margin: 0 auto 0 0; font-size: 19px; font-weight: 700; letter-spacing: -0.02em; }
			  .toolbar h2 span { color: var(--muted); font-weight: 500; font-size: 15px; margin-left: 4px; }
			  .search { position: relative; }
			  .search input { width: 220px; max-width: 100%; height: 36px; padding: 0 12px 0 34px; border-radius: 10px; border: 1px solid var(--border);
			                  background: var(--surface); color: var(--text); font: inherit; font-size: 13.5px; outline: none; transition: border-color .2s, box-shadow .2s; }
			  .search input:focus { border-color: var(--accent); box-shadow: 0 0 0 3px var(--accent-soft); }
			  .search svg { position: absolute; left: 11px; top: 50%; transform: translateY(-50%); color: var(--faint); }
			  .segmented { display: inline-flex; padding: 3px; border-radius: 11px; background: var(--surface); border: 1px solid var(--border); }
			  .segmented button { border: 0; background: transparent; color: var(--muted); font: inherit; font-size: 13px; font-weight: 600;
			                      padding: 6px 12px; border-radius: 8px; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; }
			  .segmented button em { font-style: normal; font-size: 11px; padding: 0 6px; border-radius: 999px; background: var(--code); color: var(--muted); }
			  .segmented button.active { background: var(--accent); color: #fff; }
			  .segmented button.active em { background: rgba(255,255,255,.22); color: #fff; }
			  .ghost { height: 36px; padding: 0 12px; border-radius: 10px; border: 1px solid var(--border); background: var(--surface);
			           color: var(--text); font: inherit; font-size: 13px; font-weight: 600; cursor: pointer; }
			  .ghost:hover, .segmented button:not(.active):hover { color: var(--text); background: var(--surface-2); }

			  /* ---------- Test cards ---------- */
			  .test { background: var(--surface); border: 1px solid var(--border); border-radius: 16px; margin-bottom: 12px;
			          box-shadow: var(--shadow); overflow: hidden; animation: rise .45s cubic-bezier(.2,.8,.2,1) both;
			          animation-delay: calc(var(--i) * 40ms); }
			  @keyframes rise { from { opacity: 0; transform: translateY(8px); } }
			  .test > summary { list-style: none; cursor: pointer; display: flex; align-items: center; gap: 14px; padding: 16px 18px; transition: background .15s; }
			  .test > summary::-webkit-details-marker { display: none; }
			  .test > summary:hover { background: var(--surface-2); }
			  .status-icon { flex: none; width: 34px; height: 34px; border-radius: 10px; display: grid; place-items: center; font-weight: 800; font-size: 15px; }
			  .status-icon.pass { background: var(--pass-soft); color: var(--pass); }
			  .status-icon.fail { background: var(--fail-soft); color: var(--fail); }
			  .status-icon.skip { background: var(--skip-soft); color: var(--skip); }
			  .title { flex: 1; min-width: 0; }
			  .title b { display: block; font-weight: 600; font-size: 15px; overflow-wrap: anywhere; }
			  .title small { display: block; color: var(--muted); font-size: 12px; overflow-wrap: anywhere; }
			  .dur { display: flex; flex-direction: column; align-items: flex-end; gap: 6px; width: 120px; flex: none; }
			  .dur-text { font-size: 13px; font-weight: 600; font-variant-numeric: tabular-nums; }
			  .dur-bar { width: 100%; height: 4px; border-radius: 999px; background: var(--code); overflow: hidden; }
			  .dur-bar i { display: block; height: 100%; border-radius: 999px; background: var(--accent); }
			  .test.fail .dur-bar i { background: var(--fail); } .test.skip .dur-bar i { background: var(--skip); }
			  .chev { flex: none; width: 20px; height: 20px; border-radius: 6px; position: relative; color: var(--faint); }
			  .chev::before { content: ""; position: absolute; left: 6px; top: 5px; width: 7px; height: 7px; border-right: 2px solid currentColor;
			                  border-bottom: 2px solid currentColor; transform: rotate(45deg); transition: transform .2s; }
			  .test[open] .chev::before { transform: rotate(-135deg); top: 9px; }
			  .body { padding: 4px 18px 20px 66px; border-top: 1px solid var(--border); }
			  .desc { margin: 16px 0 0; font-size: 14px; color: var(--text); }
			  .meta { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 10px; margin: 14px 0 4px; }
			  .meta div { background: var(--surface-2); border: 1px solid var(--border); border-radius: 10px; padding: 8px 12px; min-width: 0; }
			  .meta span { display: block; font-size: 11px; text-transform: uppercase; letter-spacing: .08em; color: var(--muted); font-weight: 600; }
			  .meta b { display: block; font-size: 13px; font-weight: 600; overflow-wrap: anywhere; }

			  .error-box { margin-top: 16px; border-radius: 12px; padding: 14px 16px; background: var(--fail-soft); border: 1px solid color-mix(in srgb, var(--fail) 30%, transparent); }
			  .error-box.skip { background: var(--skip-soft); border-color: color-mix(in srgb, var(--skip) 30%, transparent); }
			  .error-title { font-weight: 700; font-size: 13px; color: var(--fail); margin-bottom: 6px; }
			  .error-box.skip .error-title { color: var(--skip); }
			  .error-box pre { margin: 0; white-space: pre-wrap; overflow-wrap: anywhere; font-size: 12.5px; line-height: 1.6; }
			  .trace { margin-top: 10px; }
			  .trace summary { cursor: pointer; font-size: 12.5px; font-weight: 600; color: var(--accent); }
			  .trace pre { margin-top: 8px; padding: 12px; border-radius: 8px; background: var(--surface); max-height: 320px; overflow: auto; color: var(--muted); }

			  .steps-head { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 8px; margin: 20px 0 10px; }
			  h4 { margin: 0; font-size: 12px; text-transform: uppercase; letter-spacing: .1em; color: var(--muted); font-weight: 700; }
			  h4 span { margin-left: 6px; padding: 1px 8px; border-radius: 999px; background: var(--code); color: var(--text); font-size: 11px; }
			  .chips { display: flex; flex-wrap: wrap; gap: 6px; }
			  .chip { font-size: 12px; font-weight: 600; padding: 3px 10px; border-radius: 999px; background: var(--accent-soft); color: var(--accent); }
			  .chip.ok { background: var(--pass-soft); color: var(--pass); } .chip.bad { background: var(--fail-soft); color: var(--fail); }
			  .steps { list-style: none; margin: 0; padding: 6px; border-radius: 12px; background: var(--code); border: 1px solid var(--border);
			           max-height: 460px; overflow: auto; counter-reset: step; }
			  .step { counter-increment: step; display: flex; align-items: center; gap: 10px; padding: 7px 10px 7px 44px; border-radius: 8px;
			          position: relative; font-size: 13px; min-width: 0; overflow-wrap: anywhere; }
			  .step::before { content: counter(step); position: absolute; left: 8px; width: 26px; text-align: right; font-size: 11px;
			                  color: var(--faint); font-family: "JetBrains Mono", ui-monospace, monospace; }
			  .step:hover { background: var(--surface); }
			  .step.req { margin-top: 4px; }
			  .step.req:first-child { margin-top: 0; }
			  .method { flex: none; font: 700 10.5px/1 "JetBrains Mono", ui-monospace, monospace; letter-spacing: .04em; padding: 5px 7px; border-radius: 6px;
			            background: var(--accent-soft); color: var(--accent); }
			  .method.post { background: var(--pass-soft); color: var(--pass); } .method.delete { background: var(--fail-soft); color: var(--fail); }
			  .method.put, .method.patch { background: var(--skip-soft); color: var(--skip); }
			  .step code { font-size: 12.5px; min-width: 0; }
			  .host { color: var(--faint); } .path { color: var(--text); font-weight: 500; } .query { color: var(--accent); }
			  .code { flex: none; font: 700 11px/1 "JetBrains Mono", ui-monospace, monospace; padding: 5px 7px; border-radius: 6px; }
			  .status.ok .code { background: var(--pass-soft); color: var(--pass); } .status.bad .code { background: var(--fail-soft); color: var(--fail); }
			  .status { color: var(--muted); }
			  .tick { flex: none; width: 20px; height: 20px; border-radius: 50%; display: grid; place-items: center; font-size: 11px; font-weight: 800; }
			  .check.ok .tick { background: var(--pass-soft); color: var(--pass); }
			  .check.bad .tick { background: var(--fail); color: #fff; }
			  .check.bad { background: var(--fail-soft); }
			  .dot { flex: none; width: 6px; height: 6px; margin: 0 7px; border-radius: 50%; background: var(--faint); }
			  .info { color: var(--muted); }

			  .empty, .no-match { text-align: center; color: var(--muted); padding: 48px 16px; background: var(--surface);
			                      border: 1px dashed var(--border); border-radius: 16px; }
			  .no-match { display: none; }
			  footer { margin: 40px 0 48px; text-align: center; font-size: 12.5px; color: var(--faint); }
			  footer b { color: var(--muted); font-weight: 600; }

			  @media (max-width: 860px) {
			    .summary { grid-template-columns: 1fr; }
			    .stats, .facts { grid-template-columns: repeat(2, 1fr); }
			  }
			  @media (max-width: 560px) {
			    .hero { padding-bottom: 100px; }
			    .body { padding-left: 18px; }
			    .dur { width: 72px; }
			    .search, .search input { width: 100%; }
			    .toolbar h2 { width: 100%; }
			  }
			  @media (prefers-reduced-motion: reduce) { * { animation: none !important; transition: none !important; } }
			  @media print {
			    .icon-btn, .toolbar .search, .segmented, .ghost { display: none; }
			    .hero { padding-bottom: 32px; } .summary { margin-top: 16px; }
			    .test { break-inside: avoid; box-shadow: none; } .steps { max-height: none; }
			  }
			</style>
			</head>
			<body>
			<section class="hero">
			  <div class="wrap">
			    <div class="topbar">
			      <div class="brand">
			        <span class="logo"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M4 17l6-6-6-6"/><path d="M12 19h8"/></svg></span>
			        api-automation · Test Report
			      </div>
			      <button class="icon-btn" id="theme" title="Toggle light / dark theme" aria-label="Toggle theme">
			        <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z"/></svg>
			      </button>
			    </div>
			    <div class="hero-main">
			      <div>
			        <div class="eyebrow">Test Suite</div>
			        <h1>{{SUITE}}</h1>
			        <div class="hero-meta">
			          <span>Started <b>{{STARTED}}</b></span>
			          <span>Duration <b>{{DURATION}}</b></span>
			          <span>{{REQUESTS}} <b>API calls</b></span>
			          <span>{{CHECKS}} <b>checks</b></span>
			        </div>
			      </div>
			      <div class="verdict {{STATUS}}"><i></i>{{STATUS_TEXT}}</div>
			    </div>
			  </div>
			</section>

			<div class="wrap">
			  <section class="summary">
			    <div class="card ring-card">
			      <div class="ring">
			        <svg viewBox="0 0 120 120"><circle class="track" cx="60" cy="60" r="52"/>{{RING}}</svg>
			        <div class="ring-label"><div><b>{{PASS_RATE}}<small>%</small></b><span>Pass rate</span></div></div>
			      </div>
			      <div class="legend">
			        <span><i style="background:var(--pass)"></i>Passed</span>
			        <span><i style="background:var(--fail)"></i>Failed</span>
			        <span><i style="background:var(--skip)"></i>Skipped</span>
			      </div>
			    </div>
			    <div class="card stats-card">
			      <div class="stats">
			        <div class="stat total"><span class="label">Total</span><span class="value">{{TOTAL}}</span></div>
			        <div class="stat pass"><span class="label">Passed</span><span class="value">{{PASSED}}</span></div>
			        <div class="stat fail"><span class="label">Failed</span><span class="value">{{FAILED}}</span></div>
			        <div class="stat skip"><span class="label">Skipped</span><span class="value">{{SKIPPED}}</span></div>
			      </div>
			      <div class="bar" title="Passed {{PASSED}} · Failed {{FAILED}} · Skipped {{SKIPPED}}">
			        <i class="p" style="width:{{BAR_PASS}}%"></i><i class="f" style="width:{{BAR_FAIL}}%"></i><i class="s" style="width:{{BAR_SKIP}}%"></i>
			      </div>
			      <div class="facts">
			        <div class="fact"><span>Duration</span><b>{{DURATION}}</b></div>
			        <div class="fact"><span>Java</span><b>{{JAVA}}</b></div>
			        <div class="fact"><span>OS</span><b>{{OS}}</b></div>
			        <div class="fact"><span>Run by</span><b>{{USER}}</b></div>
			      </div>
			    </div>
			  </section>

			  <div class="toolbar">
			    <h2>Tests <span>{{TOTAL}}</span></h2>
			    <label class="search">
			      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><circle cx="11" cy="11" r="7"/><path d="M20 20l-3.5-3.5"/></svg>
			      <input id="search" type="search" placeholder="Search tests" aria-label="Search tests">
			    </label>
			    <div class="segmented" role="group" aria-label="Filter by status">
			      <button class="active" data-filter="all">All <em>{{TOTAL}}</em></button>
			      <button data-filter="pass">Passed <em>{{PASSED}}</em></button>
			      <button data-filter="fail">Failed <em>{{FAILED}}</em></button>
			      <button data-filter="skip">Skipped <em>{{SKIPPED}}</em></button>
			    </div>
			    <button class="ghost" id="toggle-all">Expand all</button>
			  </div>

			  <main id="tests">{{ROWS}}<div class="no-match" id="no-match">No tests match the current filter.</div></main>

			  <footer>Generated by <b>HtmlReportListener</b> · {{STARTED}}</footer>
			</div>

			<script>
			  (function () {
			    var root = document.documentElement;

			    // Theme: remember the viewer's choice; fall back to the system theme
			    try { var saved = localStorage.getItem('report-theme'); if (saved) root.dataset.theme = saved; } catch (e) {}
			    document.getElementById('theme').addEventListener('click', function () {
			      var dark = root.dataset.theme ? root.dataset.theme === 'dark' : matchMedia('(prefers-color-scheme: dark)').matches;
			      root.dataset.theme = dark ? 'light' : 'dark';
			      try { localStorage.setItem('report-theme', root.dataset.theme); } catch (e) {}
			    });

			    // Status filter + text search, combined
			    var tests = Array.prototype.slice.call(document.querySelectorAll('.test'));
			    var status = 'all';
			    var search = document.getElementById('search');
			    function apply() {
			      var q = search.value.trim().toLowerCase();
			      var shown = 0;
			      tests.forEach(function (t) {
			        var ok = (status === 'all' || t.dataset.status === status) && (!q || t.dataset.name.indexOf(q) !== -1);
			        t.style.display = ok ? '' : 'none';
			        if (ok) shown++;
			      });
			      document.getElementById('no-match').style.display = tests.length && !shown ? 'block' : 'none';
			    }
			    document.querySelectorAll('.segmented button').forEach(function (b) {
			      b.addEventListener('click', function () {
			        document.querySelectorAll('.segmented button').forEach(function (x) { x.classList.remove('active'); });
			        b.classList.add('active');
			        status = b.dataset.filter;
			        apply();
			      });
			    });
			    search.addEventListener('input', apply);

			    // Expand / collapse every test card
			    var toggle = document.getElementById('toggle-all');
			    toggle.addEventListener('click', function () {
			      var open = toggle.textContent === 'Expand all';
			      tests.forEach(function (t) { t.open = open; });
			      toggle.textContent = open ? 'Collapse all' : 'Expand all';
			    });
			  })();
			</script>
			</body>
			</html>
			""";
}
