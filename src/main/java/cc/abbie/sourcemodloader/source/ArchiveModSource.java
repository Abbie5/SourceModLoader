package cc.abbie.sourcemodloader.source;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.QuiltLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ArchiveModSource extends ModSource {
	public String sha256;

	private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveModSource.class);

	@Override
	public void downloadSource(File outDir) throws IOException {
		// download archive to global cache
		Path cacheDir = QuiltLoader.getCacheDir();

		URL urlUrl = new URL(url);

		File dlFile = cacheDir.resolve("sourcemodloader").resolve("download_cache").resolve(name).resolve(urlUrl.getPath()).toFile();

		if (!dlFile.exists()) {
			ReadableByteChannel rbc = Channels.newChannel(urlUrl.openStream());
			FileOutputStream fos = new FileOutputStream(dlFile);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		}

		InputStream fis = new BufferedInputStream(new FileInputStream(dlFile));
		String fileHash = DigestUtils.sha256Hex(fis);
		if (!fileHash.equals(sha256)) {
			LOGGER.error("file " + url + " does not match expected hash!\n" +
				"expected: " + sha256 + "\n" +
				"got:      " + fileHash);
		}

		// we need to read the file again
		fis = new BufferedInputStream(new FileInputStream(dlFile));

		ArchiveInputStream ais = getArchiveInputStream(dlFile, fis);
		extractArchive(ais, outDir);
	}

	private static void extractArchive(ArchiveInputStream ais, File dest) throws IOException {
		ArchiveEntry entry;
		while ((entry = ais.getNextEntry()) != null) {
			if (!ais.canReadEntryData(entry)) {
				throw new IOException("could not read archive entry: " + entry);
			}
			Path entryPath = dest.toPath().resolve(entry.getName());
			File entryFile = entryPath.toFile();
			if (entry.isDirectory()) {
				if (!entryFile.isDirectory() && !entryFile.mkdirs()) {
					throw new IOException("failed to create directory " + entryFile);
				}
			} else {
				File parent = entryFile.getParentFile();
				if (!parent.isDirectory() && !parent.mkdirs()) {
					throw new IOException("failed to create directory " + parent);
				}
				try (OutputStream os = Files.newOutputStream(entryFile.toPath())) {
					IOUtils.copy(ais, os);
				}
			}
			entryFile.setLastModified(entry.getLastModifiedDate().getTime());
		}
	}

	@NotNull
	private static ArchiveInputStream getArchiveInputStream(File dlFile, InputStream is) throws IOException {
		String dlFileName = dlFile.getName();

		ArchiveInputStream ais;
		if (dlFileName.endsWith(".tar.gz") || dlFileName.endsWith(".tgz")) {
			ais = new TarArchiveInputStream(new GzipCompressorInputStream(is));
		} else if (dlFileName.endsWith(".tar.xz")) {
			ais = new TarArchiveInputStream(new XZCompressorInputStream(is));
		} else if (dlFileName.endsWith(".tar.bz2")) {
			ais = new TarArchiveInputStream(new BZip2CompressorInputStream(is));
		} else if (dlFileName.endsWith(".tar.zstd")) {
			ais = new TarArchiveInputStream(new ZstdCompressorInputStream(is));
		} else if (dlFileName.endsWith(".zip")) {
			ais = new ZipArchiveInputStream(is);
		} else {
			throw new IllegalArgumentException("unsupported archive format: " + dlFileName);
		}
		return ais;
	}
}
