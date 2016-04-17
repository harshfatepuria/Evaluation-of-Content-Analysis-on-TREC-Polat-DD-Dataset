package shared;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

public class FilePathSupplier implements PathSupplier {
	
	public FilePathSupplier(String baseFolder) {
		setBaseFolder(baseFolder);
	}
	
	private String baseFolder;
	private URI baseFolderUri;

	public String getBaseFolder() {
		return baseFolder;
	}

	public void setBaseFolder(String baseFolder) {
		this.baseFolder = baseFolder;
		this.baseFolderUri = Paths.get(baseFolder).toUri();
	}

	@Override
	public void applyWithAllPath(BiConsumer<Path, String> operator) throws IOException {
		Files.walk(Paths.get(baseFolder)).filter(Files::isRegularFile).forEach(path -> {
			String relativePath = getRelativePath(path);
			operator.accept(path, relativePath);
		});
	}
	
	/**
	 * Get relative path of a document from specified base folder
	 * @param path
	 * @return relative path of a document
	 */
	protected String getRelativePath(Path path) {
		return baseFolderUri.relativize(path.toUri()).toString();
	}

}
