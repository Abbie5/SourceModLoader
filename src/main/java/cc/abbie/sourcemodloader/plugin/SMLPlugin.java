package cc.abbie.sourcemodloader.plugin;

import cc.abbie.sourcemodloader.config.SMLConfig;
import cc.abbie.sourcemodloader.source.ModSource;
import com.google.gson.Gson;
import org.quiltmc.loader.api.LoaderValue;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.plugin.QuiltLoaderPlugin;
import org.quiltmc.loader.api.plugin.QuiltPluginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class SMLPlugin implements QuiltLoaderPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(SMLPlugin.class);

    @Override
    public void load(QuiltPluginContext context, Map<String, LoaderValue> previousData) {
        try {
			File modsDir = new File("smlmods");
			File reposDir = new File("smlsources");
			File configFile = QuiltLoader.getConfigDir().resolve("sourcemodloader").resolve("mods.json").toFile();

			modsDir.mkdirs();
			reposDir.mkdirs();
			configFile.getParentFile().mkdirs();
			configFile.createNewFile();

			Reader reader = Files.newBufferedReader(configFile.toPath());
			SMLConfig config = new Gson().fromJson(reader, SMLConfig.class);

			if (config.sources.isEmpty()) return; // no mods to build :shrug:

			Path buildDir = QuiltLoader.getCacheDir().resolve("sourcemodloader").resolve("build");

			for (ModSource source : config.sources) {
				String outFile = source.artifact;

				String[] parts = outFile.split("/");
				File modOutFile = modsDir.toPath().resolve(parts[parts.length - 1]).toFile();
				// TODO do more robust checking, maybe check hashes?
				if (modOutFile.exists()) continue;

				File modBuildDir = buildDir.resolve(source.name).toFile();

				source.downloadSource(modBuildDir);

				source.buildSystem.build(modBuildDir);

				File modFile = modBuildDir.toPath().resolve(outFile).toFile();
				if (!modFile.exists()) {
					LOGGER.error("build didn't produce expected mod file " + outFile);
					continue;
				}

				Files.copy(modFile.toPath(), modsDir.toPath().resolve(modFile.getName()));
			}

			context.addFolderToScan(modsDir.toPath());
        } catch (IOException e) {
			LOGGER.error("couldn't build repo");
			e.printStackTrace();
		}
    }

    @Override
    public void unload(Map<String, LoaderValue> data) {
        throw new UnsupportedOperationException("cannot unload mods!");
    }
}
