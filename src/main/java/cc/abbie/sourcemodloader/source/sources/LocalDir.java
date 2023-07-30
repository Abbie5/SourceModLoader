package cc.abbie.sourcemodloader.source.sources;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public final class LocalDir extends ModSource {
	@Override
	public File downloadSource(File outDir) throws IOException {
		if (!url.getProtocol().equals("file")) throw new IOException("invalid protocol");

		FileUtils.copyDirectory(new File(url.getPath()), outDir);

		return outDir;
	}
}
