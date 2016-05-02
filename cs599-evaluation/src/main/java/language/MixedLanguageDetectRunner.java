package language;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
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

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import shared.CommandLineHelper;

public class MixedLanguageDetectRunner {
	public static void main(String[] args) throws TikaException, IOException, SAXException {
		System.out.println("Run MixedLanguageDetection");
		TikaConfig config = new TikaConfig(LanguageDetectRunner.class.getResourceAsStream("/config/tika-config.xml"));
		Tika tika = new Tika(config);
		
		Map<String, Integer> langSameCount = new HashMap<>();
		Map<String, Integer> langDifferentCount = new HashMap<>();
		
		String baseFolder = CommandLineHelper.getArg(args, 0, "C:\\cs599\\polar-fulldump\\");
		String resultFolder = CommandLineHelper.getArg(args, 1, "C:\\cs599\\a3\\language\\multi\\");
		Integer sizeThreshold = 1000;
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
	            try {
	                Thread.sleep(200);
	                System.out.println("Shutting down ...");

	                try {
	                	writeJson(langSameCount, resultFolder, "Same");
	                	writeJson(langDifferentCount, resultFolder, "Different");
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
				
				String langTika = detectWithTika(text);
				
				int chunk = Math.min(text.length()/sizeThreshold, 10);
				
				if (chunk > 2) {
					int chunkSize = (int) Math.ceil(text.length()/chunk);
					List<String> partialLang = new ArrayList<>();
					for(final String token : Splitter.fixedLength(chunkSize).split(text)){
					    if (token.length() > sizeThreshold) {
					    	partialLang.add(detectWithTika(token));
					    }
					}
					
					String majLang = majority(partialLang);
					
					if (langTika.equals(majLang)) {
						appendCount(langSameCount, langTika);
					} else {
						appendCount(langDifferentCount, langTika);
					}
				} else {
					appendCount(langSameCount, langTika);
				}

			} catch (Exception e) {
				
			}
			
			if (langSameCount.get("en") != null && langSameCount.get("en") % 2000 == 0) {
				System.out.println("en detected " + langSameCount.get("en"));
			}
		});
	}
		
	private static String detectWithTika(String text) {
		LanguageIdentifier identifier = new LanguageIdentifier(text);
		String lang = identifier.getLanguage();
		return lang;
	}
	
	private static void appendCount(Map<String, Integer> map, String lang) {
		if (!map.containsKey(lang)) {
			map.put(lang, 1);
		} else {
			map.put(lang, map.get(lang) + 1);
		}
	}

	public static String majority(List<String> nums) {
		String candidate = "";
		int count = 0;
		for (String num : nums) {
			if (count == 0)
				candidate = num;
			if (num.equals(candidate))
				count++;
			else
				count--;
		}
		return candidate;
	}
	
	private static void writeJson(Map<String, Integer> langCount, String resultFolder, String name) throws FileNotFoundException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String countJson = gson.toJson(langCount);
		
		try(PrintWriter out = new PrintWriter(new File(resultFolder, name +".json"))) {
			out.print(countJson);
		}
	}
}
