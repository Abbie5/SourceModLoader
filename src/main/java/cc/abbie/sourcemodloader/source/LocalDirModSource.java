package cc.abbie.sourcemodloader.source;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public final class LocalDirModSource extends ModSource {
	@Override
	public void downloadSource(File outDir) throws IOException {
		if (!url.getProtocol().equals("file")) throw new IOException("invalid protocol");

		FileUtils.copyDirectory(new File(url.getPath()), outDir);
	}
}
