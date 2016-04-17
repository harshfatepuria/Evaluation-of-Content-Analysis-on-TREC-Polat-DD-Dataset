package ner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ner.corenlp.CoreNLPNERecogniser;
import org.apache.tika.parser.ner.nltk.NLTKNERecogniser;
import org.apache.tika.parser.ner.opennlp.OpenNLPNERecogniser;
import org.apache.tika.sax.ToHTMLContentHandler;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import shared.TikaExtractedTextBasedParser;

public class CompositeNERAgreementParser extends TikaExtractedTextBasedParser {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6824313974934932644L;
	private boolean showIndividualNER = true;
	private Integer minThreshold = 6;
	
	public boolean isShowIndividualNER() {
		return showIndividualNER;
	}

	public void setShowIndividualNER(boolean showIndividualNER) {
		this.showIndividualNER = showIndividualNER;
	}
	
	public Integer getMinThreshold() {
		return minThreshold;
	}

	public void setMinThreshold(Integer minThreshold) {
		this.minThreshold = minThreshold;
	}

	public void parse(InputStream inputStream, ContentHandler contentHandler, Metadata metadata,
			ParseContext parseContext) throws IOException, SAXException, TikaException {

		String text = getParsedText(inputStream, metadata);

		Map<String, Set<String>> namesNLTK = getEntitiesUsingNLTK(text);
		Map<String, Set<String>> namesOpenNLP = getEntitiesUsingOpenNLP(text);
		Map<String, Set<String>> namesCoreNLP = getEntitiesUsingCoreNLP(text);
		
		if (isShowIndividualNER()) {
			putToMetadata(namesNLTK, metadata, "NLTK_");
			putToMetadata(namesOpenNLP, metadata, "OpenNLP_");
			putToMetadata(namesCoreNLP, metadata, "CoreNLP_");
		}
		
		extractNERAgreement(metadata, namesNLTK, namesOpenNLP, namesCoreNLP);
		
		XHTMLContentHandler xhtml = new XHTMLContentHandler(contentHandler, metadata);
		extractOutput(text.trim(), xhtml);
	}
	
	private void putToMetadata(Map<String, Set<String>> names, Metadata metadata, String prefix) {
		for (Map.Entry<String, Set<String>> entry : names.entrySet()) {
			if (entry.getValue() != null) {
				String mdKey = "NER_" + prefix + entry.getKey();
				for (String name : entry.getValue()) {
					metadata.add(mdKey, name);
				}
			}
		}
	}
	
	private void extractNERAgreement(Metadata metadata, Map<String, Set<String>>... maps) {
		Set<String>[] flattenSets = new Set[maps.length];
		for(int i = 0;i < maps.length; i++) {
			flattenSets[i] = flattenSet(maps[i]);
		}
		
		List<String> combined = combine(flattenSets);
		Map<Long, List<String>> freq = calFrequency(combined);
		
		List<String> names = new ArrayList<>();
		for(long i = 3; i >= 1; i--) {
			names.addAll(freq.get(i));
			
			if (names.size() > getMinThreshold()) {
				break;
			}
		}
		
		for(String name : names) {
			metadata.add("NER_Agreement_NAMES", name);
		}
	}
	
	private Set<String> flattenSet(Map<String, Set<String>> names) {
		SortedSet<String> res = new TreeSet<>();
		
		for(Set<String> set : names.values()) {
			for (String name : set) {
				res.add(name);
			}
		}
		
		return res;
	}
	
	private List<String> combine(Set<String>... sets) {
		List<String> res = new ArrayList<>();
		for(Set<String> s : sets) {
			res.addAll(s);
		}
		
		return res;
	}
	
	private Map<Long, List<String>> calFrequency(List<String> names) {
		Map<String, Long> counts = names.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
		
		Map<Long, List<String>> ret = new HashMap<>();
		
		for (long i = 1; i <= 3; i++) {
			ret.put(i, new ArrayList<>());
		}
		
		for (Map.Entry<String, Long> entry : counts.entrySet()) {
			ret.get(entry.getValue()).add(entry.getKey());
		}
		
		return ret;
	}
	
	private void extractOutput(String content, XHTMLContentHandler xhtml) throws SAXException{
        xhtml.startDocument();
        xhtml.startElement("div");
        xhtml.characters(content);
        xhtml.endElement("div");
        xhtml.endDocument();
    }
	
	
	/**
	 * Get all entities using Stanford CoreNLP NERecognizer
	 * @param text
	 * @return
	 */
	public Map<String, Set<String>> getEntitiesUsingCoreNLP(String text) {
		Map<String, Set<String>> nerResult = getCoreNLPNERecogniser().recognise(text);
		return nerResult;
	}
	
	private static CoreNLPNERecogniser coreNLPRecognizer;
	private CoreNLPNERecogniser getCoreNLPNERecogniser() {
		if (coreNLPRecognizer == null) {
			coreNLPRecognizer = new CoreNLPNERecogniser(CoreNLPNERecogniser.NER_7CLASS_MODEL);
		}
		
		return coreNLPRecognizer;
	}
	
	/**
	 * Get all entities using Apache OpenNLP NERecognizer
	 * @param text
	 * @return
	 */
	public Map<String, Set<String>> getEntitiesUsingOpenNLP(String text) {
		Map<String, Set<String>> nerResult = getOpenNLPNERecogniser().recognise(text);
		return nerResult;
	}
	
	private static OpenNLPNERecogniser openNLPRecognizer;
	private OpenNLPNERecogniser getOpenNLPNERecogniser() {
		if (openNLPRecognizer == null) {
			openNLPRecognizer = new OpenNLPNERecogniser();
		}
		
		return openNLPRecognizer;
	}
	
	/**
	 * Get all entities using NLTK NERecognizer
	 * @param text
	 * @return
	 */
	public Map<String, Set<String>> getEntitiesUsingNLTK(String text) {
		Map<String, Set<String>> nerResult = getNLTKNERecogniser().recognise(text);
		return nerResult;
	}
	
	private NLTKNERecogniser getNLTKNERecogniser() {
		return new NLTKNERecogniser();
	}
	
	
	public static void main(String[] args) throws IOException, TikaException, SAXException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		CompositeNERAgreementParser parser = new CompositeNERAgreementParser();
		parser.setShowIndividualNER(true);
		
		Metadata metadata = new Metadata();
		try (InputStream stream = new FileInputStream("C:\\cs599\\hawking.html")) {
			parser.parse(stream, new ToHTMLContentHandler(), metadata);
		}
		
		System.out.println(gson.toJson(metadata));
		
//		String text = tika.parseToString(Paths.get("C:\\cs599\\hawking.html"));
//		Map<String, Set<String>> map;
//		
//		map = parser.getEntitiesUsingNLTK(text);
//		System.out.println("NLTK");
//		System.out.println(gson.toJson(map));
//		
//		map = parser.getEntitiesUsingCoreNLP(text);
//		System.out.println("\n\nCoreNLP");
//		System.out.println(gson.toJson(map));
//		
//		map = parser.getEntitiesUsingOpenNLP(text);
//		System.out.println("\n\nOpenNLP");
//		System.out.println(gson.toJson(map));
	}
}
