package ccaparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.ToHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import cbor.CborDocument;
import shared.AbstractParserRunner;

public class CCADetailParserRunner extends AbstractParserRunner {
	
	public CCADetailParserRunner(String baseFolder, String resultFolder) throws Exception {
		this(baseFolder, resultFolder, null);
	}
	
	public CCADetailParserRunner(String baseFolder, String resultFolder, String markerFile) throws Exception {
		setBaseFolder(baseFolder);
		setResultFolder(resultFolder);
		setMarkerFile(markerFile);
		initializeParser();
	}
	
	CCADetailParser parser;
	private void initializeParser() throws TikaException, IOException, SAXException {
//		TikaConfig config = new TikaConfig(this.getClass().getResourceAsStream("/config/tika-config.xml"));
//		tika = new Tika(config);
		parser = new CCADetailParser();
	}
	
	@Override
	protected File getResultFile(String relativePath) {
		return super.getResultFile(relativePath, ".cca");
	}
	
	@Override
	protected boolean parse(Path path, String relativePath, File resultFile, CborDocument cborDoc) throws Exception {
		Metadata metadata = new Metadata();
		ContentHandler handler = new ToHTMLContentHandler();
		ParseContext context = new ParseContext();
		context.set(CborDocument.class, cborDoc);
		
		try(InputStream stream = cborDoc == null ? new FileInputStream(path.toFile()) : cborDoc.getInputStream()){
			parser.parse(stream, handler, metadata, context);
		}
		
		metadata.add("filePath", relativePath);
		
		writeJson(resultFile, metadata);
		
		return true;
	}
	
	private void writeJson(File resultFile, Metadata metadata) throws FileNotFoundException {
		String json = getGson().toJson(metadata);
		
		SortedMetadata sortedMetadata = getGson().fromJson(json, SortedMetadata.class);
		String sortedJson = getGson().toJson(sortedMetadata);
		
		resultFile.getParentFile().mkdirs();
		try(PrintWriter out = new PrintWriter(resultFile)) {
			out.print(sortedJson);
		}
	}
	
	private class SortedMetadata {
		TreeMap<String, String[]> metadata;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("Run CCAParserRunner");
		
//		String baseFolder = "C:\\cs599\\commoncrawl\\572-team6-acadis-plain\\dk";
		String baseFolder = "C:\\cs599\\commoncrawl\\";
//		String baseFolder = "C:\\cs599\\commoncrawl\\572-team41-ade\\edu\\colorado\\sidads\\098ffee9fb503a52df4a814367c6171565480a95";
//		String baseFolder = "C:\\cs599\\commoncrawl\\572-team41-ade\\edu\\columbia\\ciesin\\sedac\\12ed613323d001a1c7d07f1c3cbd2b0696854fe9";
		String resultFolder = "C:\\cs599\\a3\\cbor_detail\\result";
		String markerFile = "C:\\cs599\\a3\\cbor_detail\\marker.txt";
		
		CCADetailParserRunner runner = new CCADetailParserRunner(baseFolder, resultFolder, markerFile);
		runner.setDocumentsInCborFormat(true);
//		runner.setFileSizeLimit(15l * 1024 * 1024);
		List<String> successPath = runner.runParser();
		System.out.println("No of files: " + successPath.size());
	}
	

	
}
