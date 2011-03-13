package ikube.integration;

import ikube.IConstants;
import ikube.logging.Logging;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.UriUtilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ClusterIntegration {

	static {
		Logging.configure();
	}

	private static final Logger LOGGER = Logger.getLogger(ClusterIntegration.class);

	public static long SLEEP = 1000 * 60 * 60;

	@Test
	public void main() throws IOException, InterruptedException {
		String clusterDirectoryPath = "./cluster";
		String classpath = System.getProperty("java.class.path");
		final List<Process> processes = new ArrayList<Process>();
		Map<String, String> environment = System.getenv();
		String[] command = { "javaw", "-cp", classpath, ClusterIntegration.class.getCanonicalName(), IConstants.SPRING_CONFIGURATION_FILE };
		int servers = 3;
		for (int i = 0; i < servers; i++) {
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			File workingDirectory = FileUtilities.getFile(clusterDirectoryPath + IConstants.SEP + i, Boolean.TRUE);
			processBuilder.directory(workingDirectory);
			processBuilder.redirectErrorStream(Boolean.TRUE);
			Map<String, String> targetEnvironment = processBuilder.environment();
			targetEnvironment.putAll(environment);
			final Process process = processBuilder.start();
			processes.add(process);
			addLogStreamReader(process);
			Thread.sleep(5000);
		}
		LOGGER.info("Going to sleep : " + Thread.currentThread().hashCode());
		Thread.sleep(SLEEP);
		LOGGER.info("Waking up : " + Thread.currentThread().hashCode());
		for (Process process : processes) {
			try {
				LOGGER.info("Destroying process : " + process);
				process.destroy();
			} catch (Exception e) {
				LOGGER.error("Exception destroying process : " + process, e);
			}
		}
	}

	private void addLogStreamReader(final Process process) {
		new Thread(new Runnable() {
			public void run() {
				InputStream inputStream = process.getInputStream();
				Reader reader = new InputStreamReader(inputStream);
				char[] chars = new char[1024];
				try {
					int read = -1;
					while ((read = reader.read(chars)) > -1) {
						StringBuilder builder = new StringBuilder();
						builder.append(process.hashCode());
						builder.append(" : ");
						builder.append(chars, 0, read);
						System.out.println(UriUtilities.stripCarriageReturn(builder.toString()));
					}
				} catch (Exception e) {
					LOGGER.error("", e);
				}
			}
		}).start();
	}

	public static void main(final String[] args) {
		String configurationFile = args[0];
		ApplicationContextManager.getApplicationContext(configurationFile);
	}

}