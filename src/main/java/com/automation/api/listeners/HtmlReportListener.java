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
 * The report shows a summary (totals, pass rate, duration) and one row per
 * test with its status, duration, the steps logged through
 * {@link Util#logInfoMessage(String)} and, for failures, the error and stack trace.
 * All CSS and JavaScript are inline, so the file can be opened or shared on its own.
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
		double passRate = total == 0 ? 0 : passed * 100.0 / total;
		// Angles of the pass-rate ring: passed segment, then failed, rest is skipped
		double passDeg = total == 0 ? 0 : passed * 360.0 / total;
		double failDeg = total == 0 ? 0 : (passed + failed) * 360.0 / total;

		StringBuilder rows = new StringBuilder();
		int index = 0;
		for (ITestResult result : results) {
			rows.append(buildTestRow(result, ++index));
		}
		if (results.isEmpty()) {
			rows.append("<p class=\"empty\">No tests were run.</p>");
		}

		return TEMPLATE
				.replace("{{SUITE}}", escape(suiteName))
				.replace("{{STATUS_CLASS}}", failed > 0 ? "fail" : "pass")
				.replace("{{STATUS_TEXT}}", failed > 0 ? failed + " failed" : "All tests passed")
				.replace("{{STARTED}}", DATE_TIME.format(Instant.ofEpochMilli(start)))
				.replace("{{DURATION}}", formatDuration(end - start))
				.replace("{{TOTAL}}", String.valueOf(total))
				.replace("{{PASSED}}", String.valueOf(passed))
				.replace("{{FAILED}}", String.valueOf(failed))
				.replace("{{SKIPPED}}", String.valueOf(skipped))
				.replace("{{PASS_RATE}}", String.format("%.0f", passRate))
				.replace("{{PASS_DEG}}", String.format(java.util.Locale.ROOT, "%.2f", passDeg))
				.replace("{{FAIL_DEG}}", String.format(java.util.Locale.ROOT, "%.2f", failDeg))
				.replace("{{JAVA}}", escape(System.getProperty("java.version")))
				.replace("{{OS}}", escape(System.getProperty("os.name")))
				.replace("{{ROWS}}", rows.toString());
	}

	/**
	 * Builds the expandable block for one test: header line plus details
	 * (description, logged steps, error and stack trace).
	 *
	 * @param result   the result of the test.
	 * @param index    the 1-based position of the test in the report.
	 * @return String  the HTML for the test.
	 */
	private String buildTestRow(ITestResult result, int index) {
		String status = statusName(result.getStatus());
		String description = result.getMethod().getDescription();
		List<String> steps = Reporter.getOutput(result);
		Throwable error = result.getThrowable();

		StringBuilder details = new StringBuilder();
		details.append("<dl class=\"meta\">")
				.append("<dt>Class</dt><dd>").append(escape(result.getTestClass().getName())).append("</dd>")
				.append("<dt>Started</dt><dd>").append(DATE_TIME.format(Instant.ofEpochMilli(result.getStartMillis()))).append("</dd>");
		if (description != null && !description.isBlank()) {
			details.append("<dt>Description</dt><dd>").append(escape(description)).append("</dd>");
		}
		details.append("</dl>");

		if (!steps.isEmpty()) {
			details.append("<h4>Steps</h4><ol class=\"steps\">");
			for (String step : steps) {
				details.append("<li>").append(escape(step)).append("</li>");
			}
			details.append("</ol>");
		}

		if (error != null) {
			details.append("<h4>").append("skip".equals(status) ? "Skip reason" : "Error").append("</h4><pre class=\"error\">").append(escape(String.valueOf(error.getMessage()))).append("</pre>")
					.append("<details class=\"trace\"><summary>Stack trace</summary><pre>").append(escape(stackTrace(error)))
					.append("</pre></details>");
		}

		return "<details class=\"test\" data-status=\"" + status + "\"" + ("fail".equals(status) ? " open" : "") + ">"
				+ "<summary>"
				+ "<span class=\"idx\">" + index + "</span>"
				+ "<span class=\"pill " + status + "\">" + status.toUpperCase() + "</span>"
				+ "<span class=\"name\">" + escape(result.getMethod().getMethodName())
				+ "<small>" + escape(result.getTestClass().getRealClass().getSimpleName()) + "</small></span>"
				+ "<span class=\"time\">" + formatDuration(result.getEndMillis() - result.getStartMillis()) + "</span>"
				+ "</summary>"
				+ "<div class=\"body\">" + details + "</div>"
				+ "</details>";
	}

	/**
	 * Counts the results with the given TestNG status.
	 */
	private int count(List<ITestResult> results, int status) {
		return (int) results.stream().filter(r -> r.getStatus() == status).count();
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
			return String.format("%.2f s", millis / 1000.0);
		}
		return String.format("%dm %02ds", millis / 60_000, (millis / 1000) % 60);
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
			<title>{{SUITE}} · API Test Report</title>
			<style>
			  :root {
			    --bg: #f5f6f8; --card: #ffffff; --text: #1d2330; --muted: #6b7385; --border: #e3e6ec;
			    --pass: #1f9d63; --pass-bg: #e3f6ec; --fail: #d64545; --fail-bg: #fde8e8;
			    --skip: #c98a12; --skip-bg: #fdf3dc; --accent: #3d5afe; --code: #f0f2f6;
			  }
			  @media (prefers-color-scheme: dark) {
			    :root {
			      --bg: #0f1218; --card: #181c24; --text: #e6e9ef; --muted: #8b93a5; --border: #2a303c;
			      --pass: #3ccf8e; --pass-bg: #143326; --fail: #ff6b6b; --fail-bg: #3a1a1d;
			      --skip: #f0b43c; --skip-bg: #3a2e14; --accent: #7c8cff; --code: #11151c;
			    }
			  }
			  * { box-sizing: border-box; }
			  body { margin: 0; background: var(--bg); color: var(--text);
			         font: 15px/1.5 -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
			  .wrap { max-width: 1080px; margin: 0 auto; padding: 32px 16px 64px; }
			  header { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 24px; }
			  h1 { margin: 0; font-size: 26px; letter-spacing: -0.02em; }
			  .sub { color: var(--muted); font-size: 13px; margin-top: 4px; }
			  .badge { padding: 6px 14px; border-radius: 999px; font-weight: 600; font-size: 13px; }
			  .badge.pass { background: var(--pass-bg); color: var(--pass); }
			  .badge.fail { background: var(--fail-bg); color: var(--fail); }
			  .summary { display: grid; grid-template-columns: 220px 1fr; gap: 16px; margin-bottom: 24px; }
			  .card { background: var(--card); border: 1px solid var(--border); border-radius: 14px; padding: 20px; }
			  .ring { width: 150px; height: 150px; margin: 0 auto; border-radius: 50%; display: grid; place-items: center;
			          background: conic-gradient(var(--pass) 0 {{PASS_DEG}}deg, var(--fail) {{PASS_DEG}}deg {{FAIL_DEG}}deg, var(--skip) {{FAIL_DEG}}deg 360deg); }
			  .ring.empty { background: var(--border); }
			  .ring-inner { width: 112px; height: 112px; border-radius: 50%; background: var(--card); display: grid; place-items: center; text-align: center; }
			  .ring-inner b { font-size: 30px; display: block; line-height: 1; }
			  .ring-inner span { font-size: 12px; color: var(--muted); }
			  .stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
			  .stat { border-radius: 12px; padding: 16px; background: var(--code); }
			  .stat b { display: block; font-size: 28px; line-height: 1.1; }
			  .stat span { font-size: 12px; text-transform: uppercase; letter-spacing: .06em; color: var(--muted); }
			  .stat.pass b { color: var(--pass); } .stat.fail b { color: var(--fail); } .stat.skip b { color: var(--skip); }
			  .env { margin-top: 14px; display: flex; flex-wrap: wrap; gap: 8px 20px; font-size: 13px; color: var(--muted); }
			  .env b { color: var(--text); font-weight: 600; }
			  .toolbar { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; margin-bottom: 12px; }
			  .toolbar h2 { font-size: 17px; margin: 0 auto 0 0; }
			  .filter { border: 1px solid var(--border); background: var(--card); color: var(--text); border-radius: 999px;
			            padding: 6px 14px; font: inherit; font-size: 13px; cursor: pointer; }
			  .filter.active { background: var(--accent); border-color: var(--accent); color: #fff; }
			  .test { background: var(--card); border: 1px solid var(--border); border-radius: 12px; margin-bottom: 10px; overflow: hidden; }
			  .test[data-status="fail"] { border-left: 4px solid var(--fail); }
			  .test[data-status="pass"] { border-left: 4px solid var(--pass); }
			  .test[data-status="skip"] { border-left: 4px solid var(--skip); }
			  .test > summary { list-style: none; cursor: pointer; display: flex; align-items: center; gap: 12px; padding: 14px 16px; }
			  .test > summary::-webkit-details-marker { display: none; }
			  .test > summary:hover { background: var(--code); }
			  .idx { color: var(--muted); font-size: 12px; min-width: 22px; }
			  .pill { font-size: 11px; font-weight: 700; letter-spacing: .05em; padding: 3px 9px; border-radius: 6px; }
			  .pill.pass { background: var(--pass-bg); color: var(--pass); }
			  .pill.fail { background: var(--fail-bg); color: var(--fail); }
			  .pill.skip { background: var(--skip-bg); color: var(--skip); }
			  .name { flex: 1; font-weight: 600; min-width: 0; overflow-wrap: anywhere; }
			  .name small { display: block; font-weight: 400; color: var(--muted); font-size: 12px; }
			  .time { color: var(--muted); font-size: 13px; font-variant-numeric: tabular-nums; white-space: nowrap; }
			  .body { padding: 4px 16px 18px 50px; border-top: 1px solid var(--border); }
			  .meta { display: grid; grid-template-columns: max-content 1fr; gap: 4px 16px; font-size: 13px; margin: 14px 0; }
			  .meta dt { color: var(--muted); } .meta dd { margin: 0; overflow-wrap: anywhere; }
			  h4 { margin: 16px 0 8px; font-size: 13px; text-transform: uppercase; letter-spacing: .06em; color: var(--muted); }
			  .steps { margin: 0; padding-left: 22px; font: 12.5px/1.6 ui-monospace, SFMono-Regular, Consolas, monospace; }
			  .steps li { padding: 2px 0; overflow-wrap: anywhere; }
			  .steps li:nth-child(odd) { background: var(--code); }
			  pre { margin: 0; padding: 12px; border-radius: 8px; background: var(--code); overflow-x: auto;
			        font: 12.5px/1.55 ui-monospace, SFMono-Regular, Consolas, monospace; white-space: pre-wrap; overflow-wrap: anywhere; }
			  pre.error { background: var(--fail-bg); color: var(--fail); }
			  .trace { margin-top: 8px; } .trace summary { cursor: pointer; font-size: 13px; color: var(--accent); margin-bottom: 6px; }
			  .empty { color: var(--muted); text-align: center; padding: 32px; }
			  footer { margin-top: 32px; text-align: center; font-size: 12px; color: var(--muted); }
			  @media (max-width: 720px) {
			    .summary { grid-template-columns: 1fr; }
			    .stats { grid-template-columns: repeat(2, 1fr); }
			    .body { padding-left: 16px; }
			  }
			</style>
			</head>
			<body>
			<div class="wrap">
			  <header>
			    <div>
			      <h1>{{SUITE}}</h1>
			      <div class="sub">API test report · {{STARTED}}</div>
			    </div>
			    <span class="badge {{STATUS_CLASS}}">{{STATUS_TEXT}}</span>
			  </header>

			  <section class="summary">
			    <div class="card">
			      <div class="ring" id="ring"><div class="ring-inner"><div><b>{{PASS_RATE}}%</b><span>pass rate</span></div></div></div>
			    </div>
			    <div class="card">
			      <div class="stats">
			        <div class="stat"><b>{{TOTAL}}</b><span>Total</span></div>
			        <div class="stat pass"><b>{{PASSED}}</b><span>Passed</span></div>
			        <div class="stat fail"><b>{{FAILED}}</b><span>Failed</span></div>
			        <div class="stat skip"><b>{{SKIPPED}}</b><span>Skipped</span></div>
			      </div>
			      <div class="env">
			        <span>Duration <b>{{DURATION}}</b></span>
			        <span>Java <b>{{JAVA}}</b></span>
			        <span>OS <b>{{OS}}</b></span>
			      </div>
			    </div>
			  </section>

			  <div class="toolbar">
			    <h2>Tests</h2>
			    <button class="filter active" data-filter="all">All</button>
			    <button class="filter" data-filter="pass">Passed</button>
			    <button class="filter" data-filter="fail">Failed</button>
			    <button class="filter" data-filter="skip">Skipped</button>
			  </div>

			  <main id="tests">{{ROWS}}</main>

			  <footer>Generated by HtmlReportListener · api-automation</footer>
			</div>
			<script>
			  // Grey out the ring when no tests ran
			  if ({{TOTAL}} === 0) document.getElementById('ring').classList.add('empty');
			  // Show only tests with the selected status
			  document.querySelectorAll('.filter').forEach(function (button) {
			    button.addEventListener('click', function () {
			      document.querySelectorAll('.filter').forEach(function (b) { b.classList.remove('active'); });
			      button.classList.add('active');
			      var filter = button.dataset.filter;
			      document.querySelectorAll('.test').forEach(function (test) {
			        test.style.display = (filter === 'all' || test.dataset.status === filter) ? '' : 'none';
			      });
			    });
			  });
			</script>
			</body>
			</html>
			""";
}
