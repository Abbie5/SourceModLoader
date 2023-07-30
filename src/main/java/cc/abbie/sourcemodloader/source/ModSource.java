package cc.abbie.sourcemodloader.source;

import java.io.File;
import java.io.IOException;

public sealed abstract class ModSource permits ArchiveModSource, GitModSource, LocalDirModSource {
	public String name;
    public BuildSystem buildSystem;
    public String url;
	public String artifact;

	public abstract void downloadSource(File outDir) throws IOException;
}
