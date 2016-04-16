package ccaparser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ner.corenlp.CoreNLPNERecogniser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import TTR.TTRAnalysis;
import cbor.CborDocument;
import shared.TikaExtractedTextBasedParser;

public class CCADetailParser extends TikaExtractedTextBasedParser {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2676188666985254484L;

	@Override
	public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context)
			throws IOException, SAXException, TikaException {
		CborDocument cborDoc = context.get(CborDocument.class);
		if (cborDoc != null) {
			addSimpleCborDetail(cborDoc, metadata);
			addUrlKeyword(cborDoc, metadata);
		}
		
		byte[] bytes = IOUtils.toByteArray(stream);
		
		try (ByteArrayInputStream tstream = new ByteArrayInputStream(bytes)){
			addTikaMimeTypeDetection(tstream, metadata);
		}
			
		Metadata parsedMetadata = new Metadata();
		try (ByteArrayInputStream tstream = new ByteArrayInputStream(bytes)) {
			String extractedText = getExtractedText(tstream, parsedMetadata);
			
			addExtractedTextDetail(extractedText, metadata);
		}
		
		try (ByteArrayInputStream tstream = new ByteArrayInputStream(bytes)) {
			String ttrText = TTRAnalysis.getRelevantText(tstream);
			addTTRTextDetail(ttrText, metadata);
			addTTRTextNER(ttrText, metadata);
		}
					
		addMetaDataSizeDetail(parsedMetadata.toString(), metadata);
		addMetaDataParserDetail(parsedMetadata, metadata);
	}
	
	private void addTikaMimeTypeDetection(InputStream stream, Metadata metadata) throws IOException {
		String type = getTika().detect(stream);
		metadata.add("tika_mediaType", type);
	}
	
	private String getExtractedText(InputStream stream, Metadata metadata) throws IOException, TikaException {
		return getTika().parseToString(stream, metadata);
	}

	private void addExtractedTextDetail(String extractedText, Metadata metadata) {
		metadata.add("tika_extractedTextLength", "" + extractedText.length());
		int sizeInByte = extractedText.getBytes().length;
		metadata.add("tika_extractedTextSize", "" + sizeInByte);
	}
	
	private void addTTRTextNER(String extractedText, Metadata metadata) {
		Map<String, Set<String>> neMap = getEntitiesUsingNER(extractedText);
		for(String key : neMap.keySet()) {
			String metadataKey = "ner_" + key;
			for (String ne : neMap.get(key)) {
				metadata.add(metadataKey, ne);
			}
		}
	}
	
	private void addTTRTextDetail(String ttrText, Metadata metadata) {
		metadata.add("ttr_extractedTextLength", "" + ttrText.length());
		int sizeInByte = ttrText.getBytes().length;
		metadata.add("ttr_extractedTextSize", "" + sizeInByte);
	}
	
	private void addMetaDataSizeDetail(String metadataText, Metadata metadata) {
		metadata.add("tika_metadataLength", "" + metadataText.length());
		int sizeInByte = metadataText.getBytes().length;
		metadata.add("tika_metadataSize", "" + sizeInByte);
	}
	
	private void addMetaDataParserDetail(Metadata parsedMetadata, Metadata metadata) {
		String[] parsers = parsedMetadata.getValues("X-Parsed-By");
		for (String p : parsers) {
			metadata.add("X-Parsed-By", p);
		}
	}
	
	private void addSimpleCborDetail(CborDocument cbor, Metadata metadata) {
		metadata.add("cca_url", cbor.getUrl());
		metadata.add("cca_status", cbor.getStatus());
		
		MediaType mediaType = cbor.getMediaType();
		String type = mediaType == null ? "application/octet-stream" : mediaType.getType() + "/" + mediaType.getSubtype();
		metadata.add("cca_mediaType", type);
		
		metadata.add("cca_fileSize", "" + cbor.getFileSize());
		metadata.add("cca_bodyLength", "" + cbor.getBodyLength());
		
		String contentLength = cbor.getContentLength();
		if (contentLength != null) {
			metadata.add("cca_contentLength", contentLength);
		}
	}
	
	private void addUrlKeyword(CborDocument cbor, Metadata metadata) throws MalformedURLException {
		List<String> keywords = getUrlKeywords(cbor.getUrl());
		
		for (String keyword : keywords) {
			metadata.add("cca_urlKeyword", keyword);
		}
		
	}
	
	private List<String> getUrlKeywords(String urlString) throws MalformedURLException {
		List<String> keywords = new ArrayList<>();
		URL url = new URL(urlString);
		
		String path = url.getPath();
		
		if (path != null && path.length() > 0) {
			String[] splitByDot = path.split("\\.");
			String splitRegex = "-|/|\\_";
			if (splitByDot.length > 0) {
				String[] splitFirst = splitByDot[0].split(splitRegex);
				for(String s : splitFirst) {
					s = s.trim().toLowerCase();
					if (s.length() > 0) {
						keywords.add(s);
					}
				}
				
				for(int i = 1; i < splitByDot.length; i++) {
					String[] split = splitByDot[i].split(splitRegex);
					for(int j = 1; j < split.length; j++) {
						String s = split[j].trim().toLowerCase();
						if (s.length() > 0) {
							keywords.add(s);
						}
					}
				}
			}
		}
		String query = url.getQuery();
		if (query != null && query.length() > 0) {
			String[] qs = query.split("\\&");
			
			for(String s : qs) {
				String[] kv = s.split("=");
				String key = "";
				
				if (kv.length == 1) {
					key = kv[0];
				} else if (kv.length > 1) {
					key = kv[1];
				}
				
				key = key.trim().toLowerCase();
				if (key.length() > 0) {
					keywords.add(key);
				}
			}
		}
		List<String> depdupeKeywords = new ArrayList<>(new LinkedHashSet<>(keywords));
		return depdupeKeywords;
	}
	
	
	/**
	 * Get all entities using Stanford CoreNLP NERecognizer
	 * @param text
	 * @return
	 */
	private Map<String, Set<String>> getEntitiesUsingNER(String text) {
		Map<String, Set<String>> nerResult = getNERecogniser().recognise(text);
		return nerResult;
	}
	
	private static CoreNLPNERecogniser nlpRecognizer;
	private CoreNLPNERecogniser getNERecogniser() {
		if (nlpRecognizer == null) {
			nlpRecognizer = new CoreNLPNERecogniser(CoreNLPNERecogniser.NER_3CLASS_MODEL);
		}
		
		return nlpRecognizer;
	}
}
