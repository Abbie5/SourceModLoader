package cc.abbie.sourcemodloader.source.buildsystems;

import java.io.File;
import java.io.IOException;

public sealed interface BuildSystem permits Gradle, Simple {
	void build(File workingDir) throws IOException;
}
