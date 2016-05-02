package sizesummary;

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

import org.apache.tika.exception.TikaException;
import org.apache.tika.mime.MediaType;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cbor.CborDocument;
import cbor.CborReader;

public class SizeSummaryRunner {
	public static void main(String[] args) throws TikaException, IOException, SAXException {
		System.out.println("Run SizeSummary");
		
		Map<String, Integer> sizeCount = new HashMap<>();
		Map<String, List<Integer>> sizeList = new HashMap<>();
		
		String baseFolder = "D:\\cs599\\commoncrawl\\crawl\\";
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
	            try {
	                Thread.sleep(200);
	                System.out.println("Shutting down ...");

	                try {
						writeJson(sizeCount, sizeList);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
		});
		
		int[] count = {0};
		Files.walk(Paths.get(baseFolder)).filter(Files::isRegularFile).forEach(path -> {
			try {
				CborDocument cborDoc = CborReader.read(path.toFile());
				MediaType mediaType = cborDoc.getMediaType();
				String type = mediaType == null ? "application/octet-stream" : mediaType.getType() + "/" + mediaType.getSubtype();
				
				Integer size = cborDoc.getFileSize();
				
				appendCount(sizeCount, type);
				appendList(sizeList, type, size);
				count[0]++;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (count[0] % 2000 == 0) {
				System.out.println("sum " + count[0]);
			}
		});
	}
	
	private static void writeJson(Map<String, Integer> sizeCount, Map<String, List<Integer>> sizeList) throws FileNotFoundException {
		String resultFolder = "C:\\cs599\\a3\\size\\";
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		String countJson = gson.toJson(sizeCount);
		try(PrintWriter out = new PrintWriter(new File(resultFolder, "size_count.json"))) {
			out.print(countJson);
		}
		
		String pathJson = gson.toJson(sizeList);
		try(PrintWriter out = new PrintWriter(new File(resultFolder, "size_list.json"))) {
			out.print(pathJson);
		}
	}
	
	private static void appendCount(Map<String, Integer> map, String type) {
		if (!map.containsKey(type)) {
			map.put(type, 1);
		} else {
			map.put(type, map.get(type) + 1);
		}
	}
	
	private static void appendList(Map<String, List<Integer>> map, String type, Integer size) {
		if (!map.containsKey(type)) {
			map.put(type, new ArrayList<>());
		}
		
		map.get(type).add(size);
	}
}

