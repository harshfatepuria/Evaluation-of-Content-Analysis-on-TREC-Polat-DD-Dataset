package shared;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

import com.google.gson.Gson;

import typedetect.FileContentTypeSummary;

public class TypeJsonPathSupplier implements PathSupplier {

	public TypeJsonPathSupplier(String baseFolder, String jsonTypeFolder) {
		this(baseFolder, jsonTypeFolder, null);
	}
	
	public TypeJsonPathSupplier(String baseFolder, String jsonTypeFolder, Integer limit) {
		setBaseFolder(baseFolder);
		setJsonTypeFolder(jsonTypeFolder);
		setLimit(limit);
	}
	
	private String baseFolder;
	private String jsonTypeFolder;
	private Integer limit;
	private Gson gson = new Gson();
	
	@Override
	public void applyWithAllPath(BiConsumer<Path, String> operator) throws Exception {
		Files.walk(Paths.get(getJsonTypeFolder())).filter(Files::isRegularFile).forEach(path -> {
			try {
				BufferedReader br = new BufferedReader(new FileReader(path.toFile()));
				FileContentTypeSummary summary = gson.fromJson(br, FileContentTypeSummary.class);
				
				Integer limit = getLimit() == null ? summary.getCount() : Math.min(getLimit(), summary.getCount());
				for(int i = 0; i < limit; i++) {
					String relativePath = summary.getFiles().get(i);
					Path absolutePath = Paths.get(getBaseFolder(), relativePath);
					
					operator.accept(absolutePath, relativePath);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}  

		});
	}

	public String getBaseFolder() {
		return baseFolder;
	}

	public void setBaseFolder(String baseFolder) {
		this.baseFolder = baseFolder;
	}

	public String getJsonTypeFolder() {
		return jsonTypeFolder;
	}

	public void setJsonTypeFolder(String jsonTypeFolder) {
		this.jsonTypeFolder = jsonTypeFolder;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	
	
}
