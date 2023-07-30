package cc.abbie.sourcemodloader.source.sources;

import cc.abbie.sourcemodloader.source.buildsystems.BuildSystem;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public sealed abstract class ModSource permits Archive, Git, LocalDir {
	public String name;
    public BuildSystem buildsystem;
    public URL url;
	public String artifact;

	/**
	 * @param outDir the source output directory
	 * @return the actual location of the source to build in
	 * @throws IOException when stuff goes wrong
	 */
	public abstract File downloadSource(File outDir) throws IOException;
}
