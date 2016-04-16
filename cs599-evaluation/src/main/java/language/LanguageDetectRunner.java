package language;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

public class LanguageDetectRunner {
	public static void main(String[] args) throws TikaException, IOException, SAXException {
		System.out.println("Run LanguageDetection");
		TikaConfig config = new TikaConfig(LanguageDetectRunner.class.getResourceAsStream("/config/tika-config.xml"));
		Tika tika = new Tika(config);
		
		Map<String, Integer> langCount = new HashMap<>();
		Map<String, List<String>> langPath = new HashMap<>();
		
		String baseFolder = "C:\\cs599\\polar-fulldump\\";
		baseFolderUri = Paths.get(baseFolder).toUri();
		
		prepareLanguageDetector();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
	            try {
	                Thread.sleep(200);
	                System.out.println("Shutting down ...");

	                try {
						writeJson(langCount, langPath);
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
				String text = tika.parseToString(path);
				String lang = detectWithLanguageDetector(text);
				
				appendCount(langCount, lang);
				
				if (!"en".equals(lang)) {
					String relativePath = getRelativePath(path);
					appendPath(langPath, lang, relativePath);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
			
			if (langCount.get("en") != null && langCount.get("en") % 2000 == 0) {
				System.out.println("en detected " + langCount.get("en"));
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
	
	private static void writeJson(Map<String, Integer> langCount, Map<String, List<String>> langPath) throws FileNotFoundException {
		String resultFolder = "C:\\cs599\\a3\\language\\";
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String countJson = gson.toJson(langCount);
		
		try(PrintWriter out = new PrintWriter(new File(resultFolder, "lang_count.json"))) {
			out.print(countJson);
		}
		
		String pathJson = gson.toJson(langPath);
		try(PrintWriter out = new PrintWriter(new File(resultFolder, "lang_path.json"))) {
			out.print(pathJson);
		}
	}
	
	static URI baseFolderUri;
	private static String getRelativePath(Path path) {
		return baseFolderUri.relativize(path.toUri()).toString();
	}
	
	private static void appendCount(Map<String, Integer> map, String lang) {
		if (!map.containsKey(lang)) {
			map.put(lang, 1);
		} else {
			map.put(lang, map.get(lang) + 1);
		}
	}
	
	private static void appendPath(Map<String, List<String>> map, String lang, String path) {
		if (!map.containsKey(lang)) {
			map.put(lang, new ArrayList<>());
		}
		
		map.get(lang).add(path);
	}
}
