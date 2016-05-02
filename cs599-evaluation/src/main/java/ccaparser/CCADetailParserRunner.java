package ccaparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.ToHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import cbor.CborDocument;
import shared.AbstractParserRunner;
import shared.CommandLineHelper;

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
	
	public static void main(String[] args) throws Exception {
		System.out.println("Run CCAParserRunner");
		
		String baseFolder = CommandLineHelper.getArg(args, 0, "C:\\cs599\\commoncrawl\\");
		String resultFolder = CommandLineHelper.getArg(args, 1, "C:\\cs599\\a3\\cbor_detail\\result");
		String markerFile = CommandLineHelper.getArg(args, 2, "C:\\cs599\\a3\\cbor_detail\\marker.txt");
		
		CCADetailParserRunner runner = new CCADetailParserRunner(baseFolder, resultFolder, markerFile);
		runner.setDocumentsInCborFormat(true);
		List<String> successPath = runner.runParser();
		System.out.println("No of files: " + successPath.size());
	}
	

	
}
