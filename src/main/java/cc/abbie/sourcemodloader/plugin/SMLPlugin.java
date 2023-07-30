package cc.abbie.sourcemodloader.plugin;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.URIish;
import org.quiltmc.loader.api.LoaderValue;
import org.quiltmc.loader.api.plugin.QuiltLoaderPlugin;
import org.quiltmc.loader.api.plugin.QuiltPluginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SMLPlugin implements QuiltLoaderPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(SMLPlugin.class);

    // TODO replace by loading from config file
    private static final Map<String, String> modsToBuild = Map.of(
            "https://github.com/Abbie5/global-options.git", "build/libs/global_options-1.0.jar"
    );

    @Override
    public void load(QuiltPluginContext context, Map<String, LoaderValue> previousData) {
        try {
            File modsDir = new File("smlmods");
            if (!modsDir.exists()) modsDir.mkdir();
            File reposDir = new File("smlsources");
            if (!reposDir.exists()) reposDir.mkdir();

            for (Map.Entry<String, String> entry : modsToBuild.entrySet()) {
                String uri = entry.getKey();
                String outFile = entry.getValue();

                String[] parts = outFile.split("/");
                File modOutFile = modsDir.toPath().resolve(parts[parts.length-1]).toFile();
                // TODO do more robust checking, maybe check hashes?
                if (modOutFile.exists()) continue;

                File repoDir = reposDir.toPath().resolve(new URIish(uri).getHumanishName()).toFile();

                if (!repoDir.exists()) {
                    Git.cloneRepository()
                            .setURI(uri)
                            .setDirectory(repoDir)
                            .call();
                }

                ProcessBuilder buildProcessBuilder = new ProcessBuilder("./gradlew", "build")
                        .directory(repoDir)
                        .inheritIO();
                Map<String, String> env = buildProcessBuilder.environment();
                String envJavaHome = System.getenv("JAVA_HOME");
                // TODO get the JAVA_HOME properly in the dev env
                String javaHome = envJavaHome != null ? envJavaHome : "/app/extra/IDEA-U/jbr";
                env.put("JAVA_HOME", javaHome);

                Process buildProcess = buildProcessBuilder.start();
                int exitCode = buildProcess.waitFor();
                if (exitCode != 0) {
                    LOGGER.error("build failed with exit code " + exitCode);
                    return;
                }

                File modFile = repoDir.toPath().resolve(outFile).toFile();
                if (!modFile.exists()) {
                    LOGGER.error("build didn't produce expected mod file " + outFile);
                }

                Files.copy(modFile.toPath(), modsDir.toPath().resolve(modFile.getName()));
            }

            context.addFolderToScan(modsDir.toPath());

        } catch (GitAPIException e) {
            LOGGER.error("couldn't clone repo");
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.error("couldn't build repo");
            e.printStackTrace();
        } catch (URISyntaxException e) {
            LOGGER.error("not a valid uri! " + e.getMessage());
        } catch (InterruptedException e) {
            LOGGER.error("build process got interrupted: " + e.getMessage());
        }
    }

    @Override
    public void unload(Map<String, LoaderValue> data) {
        throw new UnsupportedOperationException("cannot unload mods!");
    }
}
