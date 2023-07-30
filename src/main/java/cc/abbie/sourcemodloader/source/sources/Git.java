package cc.abbie.sourcemodloader.source.sources;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.URIish;

import java.io.File;
import java.io.IOException;

public final class Git extends ModSource {
    public String ref;

	@Override
	public File downloadSource(File outDir) throws IOException {
		try {
			File repoDir = outDir.toPath().resolve(new URIish(url).getHumanishName()).toFile();

			if (!repoDir.exists()) {
				org.eclipse.jgit.api.Git.cloneRepository()
					.setURI(url.toString())
					.setBranch(ref)
					.setDirectory(repoDir)
					.setProgressMonitor(new TextProgressMonitor())
					.call();
			}

			return repoDir;
		} catch (GitAPIException e) {
			throw new IOException(e);
		}
	}
}
