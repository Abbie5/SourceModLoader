package cc.abbie.sourcemodloader;

import cc.abbie.sourcemodloader.config.SMLConfig;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.config.QuiltConfig;
import org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class SourceModLoader implements PreLaunchEntrypoint {
	public static final String MODID = "sourcemodloader";
	public static SMLConfig CONFIG;
	@Override
	public void onPreLaunch(ModContainer mod) {
		CONFIG = QuiltConfig.create(MODID, "mods", SMLConfig.class);
	}
}
