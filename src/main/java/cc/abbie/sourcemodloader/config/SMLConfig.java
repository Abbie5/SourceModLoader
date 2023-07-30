package cc.abbie.sourcemodloader.config;

import org.quiltmc.config.api.WrappedConfig;
import org.quiltmc.config.api.values.ComplexConfigValue;
import org.quiltmc.config.api.values.ConfigSerializableObject;
import org.quiltmc.config.api.values.ValueList;

public class SMLConfig extends WrappedConfig {
    public final ValueList<ModSource> sources = ValueList.create(new ModSource(SourceType.GIT_REPO, "https://github.com/Abbie5/SourceModLoader.git"));

	public record ModSource(
		SourceType type,
		String uri
	) implements ConfigSerializableObject<ModSource> {
		@Override
		public ConfigSerializableObject<ModSource> convertFrom(ModSource representation) {
			return null;
		}

		@Override
		public ModSource getRepresentation() {
			return null;
		}

		@Override
		public ComplexConfigValue copy() {
			return null;
		}
	}

    public enum SourceType {
        LOCAL_FOLDER,
        REMOTE_ARCHIVE,
        GIT_REPO
    }
}
