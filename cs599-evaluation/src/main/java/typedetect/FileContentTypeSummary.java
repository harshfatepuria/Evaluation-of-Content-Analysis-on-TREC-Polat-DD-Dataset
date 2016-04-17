package typedetect;

import java.util.ArrayList;
import java.util.List;

public class FileContentTypeSummary {
	private String type;
	private List<String> files;
	
	public FileContentTypeSummary() {
		files = new ArrayList<>();
	}
	
	public FileContentTypeSummary(String type) {
		setType(type);
		files = new ArrayList<>();
	}
	
	
	public Integer getCount() {
		return getFiles().size();
	}
	
	public void setCount(Integer count) {
		
	}
	
	public void addFile(String file) {
		files.add(file);
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getFiles() {
		return files;
	}
	public void setFiles(List<String> files) {
		this.files = files;
	}
	
	
}
