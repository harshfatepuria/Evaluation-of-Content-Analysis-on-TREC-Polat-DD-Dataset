package sizesummary;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

import cbor.CborDocument;
import shared.AbstractParserRunner;

public class SizeParserRunner extends AbstractParserRunner {
	public SizeParserRunner(String baseFolder, String resultFolder) throws Exception {
		this(baseFolder, resultFolder, null);
	}
	
	public SizeParserRunner(String baseFolder, String resultFolder, String markerFile) throws Exception {
		setBaseFolder(baseFolder);
		setResultFolder(resultFolder);
		setMarkerFile(markerFile);
	}

	@Override
	protected File getResultFile(String relativePath) {
		return super.getResultFile(relativePath, ".size");
	}

	@Override
	protected boolean parse(Path path, String relativePath, File resultFile, CborDocument cborDoc) throws Exception {
		Metadata metadata = new Metadata();
		
		if (cborDoc == null) {
			return false;
		}
		
		metadata.add("filePath", relativePath);
		
		MediaType mediaType = cborDoc.getMediaType();
		String type = mediaType == null ? "application/octet-stream" : mediaType.getType() + "/" + mediaType.getSubtype();
		
		metadata.add("mediaType", type);
		
		Integer size = cborDoc.getFileSize();
		metadata.add("size", ""+ size);
		
		String json = getGson().toJson(metadata);
		
		resultFile.getParentFile().mkdirs();
		try(PrintWriter out = new PrintWriter(resultFile)) {
			out.print(json);
		}
		
		return true;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("Run SizeParserRunner");
		
		String baseFolder = "D:\\cs599\\commoncrawl";
		String resultFolder = "C:\\cs599\\a3\\size\\result";
		String markerFile = "C:\\cs599\\a3\\size\\marker.txt";
		
		SizeParserRunner runner = new SizeParserRunner(baseFolder, resultFolder, markerFile);
		runner.setDocumentsInCborFormat(true);
		
		List<String> successPath = runner.runParser();
		System.out.println("No of files: " + successPath.size());
	}
}
