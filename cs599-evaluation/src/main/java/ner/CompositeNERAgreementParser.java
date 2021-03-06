package ner;

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

import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ner.corenlp.CoreNLPNERecogniser;
import org.apache.tika.parser.ner.grobidquantity.GrobidQuantityRecogniser;
import org.apache.tika.parser.ner.nltk.NLTKNERecogniser;
import org.apache.tika.parser.ner.opennlp.OpenNLPNERecogniser;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import TTR.TTRAnalysis;
import shared.TikaExtractedTextBasedParser;

public class CompositeNERAgreementParser extends TikaExtractedTextBasedParser {

	public CompositeNERAgreementParser() throws TikaException, IOException, SAXException {
		TikaConfig tikaConfig = new TikaConfig(this.getClass().getResourceAsStream("/config/tika-config.xml"));
		Tika tika = new Tika(tikaConfig);
		setTika(tika);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6824313974934932644L;
	private boolean showIndividualNER = true;
	private Integer minThreshold = 6;
	private boolean useTTR = false;
	
	public boolean isShowIndividualNER() {
		return showIndividualNER;
	}

	/**
	 * Set whether to export all NER extracted by each recognizer
	 * @param showIndividualNER
	 */
	public void setShowIndividualNER(boolean showIndividualNER) {
		this.showIndividualNER = showIndividualNER;
	}
	
	public Integer getMinThreshold() {
		return minThreshold;
	}

	/**
	 * Set the minimum amount of NER that should be extracted by NER Agreement
	 * @param minThreshold
	 */
	public void setMinThreshold(Integer minThreshold) {
		this.minThreshold = minThreshold;
	}
	
	public boolean isUseTTR() {
		return useTTR;
	}

	/**
	 * Set whether to use TTR Analysis with extracted text before doing NER
	 * @param useTTR
	 */
	public void setUseTTR(boolean useTTR) {
		this.useTTR = useTTR;
	}

	public void parse(InputStream inputStream, ContentHandler contentHandler, Metadata metadata,
			ParseContext parseContext) throws IOException, SAXException, TikaException {

		String text = "";
		try {
			if (isUseTTR()) {
				text = TTRAnalysis.getRelevantText(inputStream, metadata).trim();
			} else {
				text = getParsedText(inputStream, metadata).trim();
			}
		}
		catch (Exception e) {
			return;
		}
		
		if (text.length() > 0) {
			Map<String, Set<String>> namesNLTK = null;
			Map<String, Set<String>> namesOpenNLP = null;
			Map<String, Set<String>> namesCoreNLP = null;
			
			namesNLTK = getEntitiesUsingNLTK(text);
			namesOpenNLP = getEntitiesUsingOpenNLP(text);
			namesCoreNLP = getEntitiesUsingCoreNLP(text);
			
			if (isShowIndividualNER()) {
				putToMetadata(namesNLTK, metadata, "NLTK_");
				putToMetadata(namesOpenNLP, metadata, "OpenNLP_");
				putToMetadata(namesCoreNLP, metadata, "CoreNLP_");
			}
			
			extractNERAgreement(metadata, namesNLTK, namesOpenNLP, namesCoreNLP);
		}
		
		XHTMLContentHandler xhtml = new XHTMLContentHandler(contentHandler, metadata);
		extractOutput(text, xhtml);
	}
	
	private void putToMetadata(Map<String, Set<String>> names, Metadata metadata, String prefix) {
		if (names == null) {
			return;
		}
		
		for (Map.Entry<String, Set<String>> entry : names.entrySet()) {
			if (entry.getValue() != null) {
				String mdKey = "NER_" + prefix + entry.getKey();
				for (String name : entry.getValue()) {
					metadata.add(mdKey, name);
				}
			}
		}
	}
	
	/**
	 * Extract the NER Agreement from all of the recognizers
	 * @param metadata
	 * @param maps
	 */
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
		
		if (names != null) {
			for(Set<String> set : names.values()) {
				for (String name : set) {
					res.add(name);
				}
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
	 * Get all entities using Grobid Quantity Recognizer
	 * @param text
	 * @return
	 */
	public Map<String, Set<String>> getEntitiesUsingGrobidQuantity(String text) {
		Map<String, Set<String>> nerResult = getGrobidQuantityRecogniser().recognise(text);
		return nerResult;
	}
	
	private GrobidQuantityRecogniser getGrobidQuantityRecogniser() {
		return new GrobidQuantityRecogniser();
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
		
		/*
		parser.setShowIndividualNER(true);
		
		Metadata metadata = new Metadata();
		try (InputStream stream = new FileInputStream("C:\\cs599\\hawking.html")) {
			parser.parse(stream, new ToHTMLContentHandler(), metadata);
		}
		
		System.out.println(gson.toJson(metadata));
		*/
		
		
		
		
//		String text = TTRAnalysis.getRelevantText("/Users/harshfatepuria/Desktop/599/HW3/test.html");
//		Map<String, Set<String>> map;
//		
//		map = parser.getEntitiesUsingNLTK(text);
//		System.out.println("NLTK");
//		System.out.println(gson.toJson(map));
		
		
		String s = TTRAnalysis.getRelevantText("/Users/harshfatepuria/Desktop/Internship_CoverLetter.docx");
		s=s.trim();
		int i;
		String[] str_array = s.split("\n");
		
		
		for(i=0; i<str_array.length; i++)
		{
			System.out.println();
			System.out.println(str_array[i]);
			GrobidQuantityRecogniser obj = new GrobidQuantityRecogniser();
			obj.recognise(str_array[i]);
		}
		
		
		
		
		
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
