package ner;

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
import shared.TypeJsonPathSupplier;

public class NERAgreementParserRunner extends AbstractParserRunner {

	public NERAgreementParserRunner(String baseFolder, String resultFolder) throws Exception {
		this(baseFolder, resultFolder, null);
	}
	
	public NERAgreementParserRunner(String baseFolder, String resultFolder, String markerFile) throws Exception {
		setBaseFolder(baseFolder);
		setResultFolder(resultFolder);
		setMarkerFile(markerFile);
		initializeParser();
	}
	
	CompositeNERAgreementParser parser;
	private void initializeParser() throws TikaException, IOException, SAXException {
		parser = new CompositeNERAgreementParser();
	}
	
	@Override
	protected File getResultFile(String relativePath) {
		return super.getResultFile(relativePath, ".ner");
	}
	
	public CompositeNERAgreementParser getParser() {
		return parser;
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
		System.out.println("Run NERAgreementParserRunner");
		
		String baseFolder = "C:\\cs599\\polar-fulldump\\";
		String resultFolder = "C:\\cs599\\a3\\ner\\result";
		String markerFile = "C:\\cs599\\a3\\ner\\marker.txt";
		String jsonFolder = "C:\\cs599\\polar-json\\byType";
		
		TypeJsonPathSupplier pathSupplier = new TypeJsonPathSupplier(baseFolder, jsonFolder, 1500);
		NERAgreementParserRunner runner = new NERAgreementParserRunner(baseFolder, resultFolder, markerFile);
		runner.setPathSupplier(pathSupplier);
		runner.getParser().setMinThreshold(6);
		
		List<String> successPath = runner.runParser();
		System.out.println("No of files: " + successPath.size());
	}

}
