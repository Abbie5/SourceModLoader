package cc.abbie.sourcemodloader.source.buildsystems;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public final class Gradle implements BuildSystem {
	private static final Logger LOGGER = LoggerFactory.getLogger(Gradle.class);

	private static final String gradleCmd;

	static {
		if (SystemUtils.IS_OS_UNIX) gradleCmd = "./gradlew";
		else if (SystemUtils.IS_OS_WINDOWS) gradleCmd = "gradlew.bat";
		else gradleCmd = "gradle"; // idk lol
	}

	@Override
	public void build(File workingDir) throws IOException {
		ProcessBuilder buildProcessBuilder = new ProcessBuilder(gradleCmd, "build")
			.directory(workingDir)
			.inheritIO();
		Map<String, String> env = buildProcessBuilder.environment();
		String envJavaHome = System.getenv("JAVA_HOME");
		// TODO get the JAVA_HOME properly in the dev env
		String javaHome = envJavaHome != null ? envJavaHome : "/app/extra/IDEA-U/jbr";
		env.put("JAVA_HOME", javaHome);

		Process buildProcess = buildProcessBuilder.start();
		try {
			int exitCode = buildProcess.waitFor();
			if (exitCode != 0) {
				LOGGER.error("build failed with exit code " + exitCode);
			}
		} catch (InterruptedException e) {
			LOGGER.error("build was interrupted: " + e.getMessage());
		}
	}
}
