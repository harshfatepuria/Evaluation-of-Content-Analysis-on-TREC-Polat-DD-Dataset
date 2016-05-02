package ner;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import TTR.TTRAnalysis;
import java.io.PrintWriter;

import org.apache.tika.exception.TikaException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.xml.sax.SAXException;
public class GrobidQuantitiesNER
{
	public static void main(String args[]) throws TikaException, IOException, SAXException{
		String polarData="/Volumes/ETC/polar-fulldump/";
		String listOfAllFiles="/Volumes/ETC/599/HW2/FINAL FILES/listOfAllFiles.json";
		String jsonFolder="/Volumes/ETC/599/HW2/FINAL FILES/fulldump-path-all-json/";
		String result="/Users/harshfatepuria/Desktop/599/HW3/";
		int i,j,k;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		CompositeNERAgreementParser parser = new CompositeNERAgreementParser();
		Map<String, Integer> countOfMeasurements = new HashMap<String,Integer>();
		countOfMeasurements.put("deg", 0);
		PrintWriter writer=null;
		try {
			
			
			writer = new PrintWriter(result+"grobid_quantity_output1.json", "UTF-8");
			FileReader reader = new FileReader(listOfAllFiles);
	        JSONParser jsonParser = new JSONParser();
	        JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
	        JSONArray lang= (JSONArray) jsonObject.get("files");
	         
	        for(i=0; i<lang.size(); i++){
	        	String fileName=lang.get(i).toString();
	        	System.out.println(fileName);
	        	FileReader reader2 = new FileReader(jsonFolder+fileName);
	            JSONParser jsonParser2 = new JSONParser();
	            JSONObject jsonObject2 = (JSONObject) jsonParser2.parse(reader2);

	            JSONArray lang2= (JSONArray) jsonObject2.get("files");
	            int count=(lang2.size()<1500)?lang2.size():1500;
	            System.out.println(count);
	        	for(j=0; j<count; j++)
	        	{
	        		try{
	        		String s = TTRAnalysis.getRelevantText(polarData+lang2.get(j).toString());
	        		Map<String, Set<String>> map;
	        		map=parser.getEntitiesUsingGrobidQuantity(s);
	        		JSONParser jsonParser3 = new JSONParser();
	   	         	JSONObject jsonObject3 = (JSONObject) jsonParser3.parse(gson.toJson(map));
	   	         	JSONArray lang3= (JSONArray) jsonObject3.get("measurements");
	   	         	for(k=0; k<lang3.size(); k++)
	   	         	{
	   	         		String type="";
	   	         		String rawUnitName="";
	   	         		JSONParser jsonParser4 = new JSONParser();
	   	         		JSONObject eachMeasurement = (JSONObject) jsonParser4.parse(lang3.get(k).toString());
	   	         		JSONObject quantity = (JSONObject) jsonParser4.parse(eachMeasurement.get("quantity").toString());
	   	         		if(quantity.get("type")!=null)
	   	         		{
	   	         			type=quantity.get("type").toString();
	   	         			if(countOfMeasurements.get(type)!=null){
	   	         				int qq=countOfMeasurements.get(type);
	   	         				countOfMeasurements.replace(type, qq,qq+1);
	   	         			}
	   	         			else{
	   	         				countOfMeasurements.put(type, 1);
	   	         			}
	   	         		}
	   	         		if(quantity.get("rawUnit")!=null)
	   	         		{
	   	         			JSONObject rawUnit = (JSONObject) jsonParser4.parse(quantity.get("rawUnit").toString());
	   	         			rawUnitName=rawUnit.get("name").toString();
	   	         			
	   	         			if(countOfMeasurements.get(rawUnitName)!=null){
	   	         				
	   	         				int qq=countOfMeasurements.get(rawUnitName);
	   	         				countOfMeasurements.replace(rawUnitName, qq,qq+1);
	   	         			}
	   	         			else{
	   	         				countOfMeasurements.put(rawUnitName, 1);
	   	         			}
	   	         		}
	   	         	}
	        		}
	        		catch(Exception e)
	        		{
	        			continue;
	        		}
	        	}
	        }
		}
		catch(Exception e)
		{
			System.out.println("BAD EXCEPTION!");
		}
		finally{
			System.out.println(gson.toJson(countOfMeasurements));
			writer.println(gson.toJson(countOfMeasurements));
			writer.close(); 
		}
	}
	
}
