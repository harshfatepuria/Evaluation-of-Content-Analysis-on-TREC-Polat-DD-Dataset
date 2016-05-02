package language;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.language.LanguageIdentifier;
import org.xml.sax.SAXException;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;

import shared.CommandLineHelper;

public class LanguageDetectRunner {
	public static void main(String[] args) throws TikaException, IOException, SAXException {
		System.out.println("Run LanguageDetection");
		TikaConfig config = new TikaConfig(LanguageDetectRunner.class.getResourceAsStream("/config/tika-config.xml"));
		Tika tika = new Tika(config);
		
		Map<String, Integer> langCountTika = new HashMap<>();
		Map<String, Integer> langCountLd = new HashMap<>();
		
		String baseFolder = CommandLineHelper.getArg(args, 0, "C:\\cs599\\polar-fulldump\\");
		String resultFolder = CommandLineHelper.getArg(args, 1, "C:\\cs599\\a3\\language\\compare\\");
		
		String sizeThresholdString = CommandLineHelper.getArg(args, 2, "-1");
		Integer sizeThreshold = Integer.parseInt(sizeThresholdString);
		
		prepareLanguageDetector();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
	            try {
	                Thread.sleep(200);
	                System.out.println("Shutting down ...");

	                try {
						writeJson(langCountTika, resultFolder, "Tika");
						writeJson(langCountLd, resultFolder, "Optimaize");
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
		});
		
		Files.walk(Paths.get(baseFolder)).filter(Files::isRegularFile).forEach(path -> {
			try {
				if (path.toFile().length() > 1024*1024) {
					return;
				}
				
				String text = tika.parseToString(path);
				
				if (text.length() < sizeThreshold) {
					return;
				}
				
				String langLd = detectWithLanguageDetector(text);
				appendCount(langCountLd, langLd);
				
				String langTika = detectWithTika(text);
				appendCount(langCountTika, langTika);
				
				/*
				if (!"en".equals(lang)) {
					String relativePath = getRelativePath(path);
					appendPath(langPath, lang, relativePath);
				}
				*/
			} catch (Exception e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
			
			if (langCountLd.get("en") != null && langCountLd.get("en") % 2000 == 0) {
				System.out.println("en detected " + langCountLd.get("en"));
			}
		});
		
	}
	
	private static String detectWithTika(String text) {
		LanguageIdentifier identifier = new LanguageIdentifier(text);
		String lang = identifier.getLanguage();
		return lang;
	}
	
	private static LanguageDetector languageDetector;
	private static void prepareLanguageDetector() throws IOException {
		List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();

		//build language detector:
		languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
		        .withProfiles(languageProfiles)
		        .build();

	}
	
	private static String detectWithLanguageDetector(String text) {
		//create a text object factory
		TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();

		//query:
		TextObject textObject = textObjectFactory.forText(text);
		Optional<LdLocale> lang = languageDetector.detect(textObject);
		
		if (lang.isPresent()) {
			return lang.get().getLanguage();
		} else {
			return "unknown";
		}
	}
	
	private static void writeJson(Map<String, Integer> langCount, String resultFolder, String name) throws FileNotFoundException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String countJson = gson.toJson(langCount);
		
		try(PrintWriter out = new PrintWriter(new File(resultFolder, name +".json"))) {
			out.print(countJson);
		}
	}
	
	private static void appendCount(Map<String, Integer> map, String lang) {
		if (!map.containsKey(lang)) {
			map.put(lang, 1);
		} else {
			map.put(lang, map.get(lang) + 1);
		}
	}
	
	/*
	static URI baseFolderUri;
	private static String getRelativePath(Path path) {
		return baseFolderUri.relativize(path.toUri()).toString();
	}
	
	private static void appendPath(Map<String, List<String>> map, String lang, String path) {
		if (!map.containsKey(lang)) {
			map.put(lang, new ArrayList<>());
		}
		
		map.get(lang).add(path);
	}
	*/
}
