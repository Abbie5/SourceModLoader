package cc.abbie.sourcemodloader.source;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public final class GitModSource extends ModSource {
    public String ref;

	@Override
	public void downloadSource(File outDir) throws IOException {
		try {
			File repoDir = outDir.toPath().resolve(new URIish(url).getHumanishName()).toFile();

			if (!repoDir.exists()) {
				Git.cloneRepository()
					.setURI(url)
					.setBranch(ref)
					.setDirectory(repoDir)
					.call();
			}
		} catch (GitAPIException | URISyntaxException e) {
			throw new IOException(e);
		}
	}
}
