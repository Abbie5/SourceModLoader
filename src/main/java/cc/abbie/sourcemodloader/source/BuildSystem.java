package cc.abbie.sourcemodloader.source;

import java.io.File;
import java.io.IOException;

public sealed interface BuildSystem permits Gradle {
	void build(File workingDir) throws IOException;
}
