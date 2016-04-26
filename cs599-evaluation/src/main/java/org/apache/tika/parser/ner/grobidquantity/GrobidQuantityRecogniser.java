//
///**
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.apache.tika.parser.ner.grobidquantity;
//
//
//import java.io.BufferedReader;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
//
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;
//
//import org.apache.cxf.jaxrs.client.WebClient;
//
//public class GrobidQuantityRecogniser {
//
//  private static final String GROBID_REST_HOST = "http://localhost:8080";
//
//  private static final String GROBID_QUANTITY_PROCESSHEADER_PATH = "/processQuantityText";
//
//  private String restHostUrlStr;
//
//  public GrobidQuantityRecogniser() {
//      this.restHostUrlStr = GROBID_REST_HOST;
//      Response response = WebClient.create(restHostUrlStr).accept(MediaType.TEXT_HTML).get();
//      int responseCode = response.getStatus();
//      if(responseCode != 200){
//         System.out.println("Grobid Quantities REST Server not running.");
//      }
//  }
//
//  public void parse(String text) throws UnsupportedEncodingException
//  {
//	Response response = WebClient.create(restHostUrlStr + GROBID_QUANTITY_PROCESSHEADER_PATH + "?text=" + URLEncoder.encode(text,"UTF-8")).accept(MediaType.APPLICATION_JSON).get();
//	try
//	{
//      String resp = response.readEntity(String.class);
//      System.out.println(resp.toString());
//    }
//	catch (Exception e) 
//	{
//      e.printStackTrace();
//    }
//  }
//  
//
//  public static void main(String args[]) throws IOException, InterruptedException
//  {
//	  GrobidQuantityRecogniser obj = new GrobidQuantityRecogniser();
//	  obj.parse("A 20 kg time");
//	  
//	  
//  }
//
//}
//



/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tika.parser.ner.grobidquantity;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Properties;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;

public class GrobidQuantityRecogniser {

  private static final String GROBID_REST_HOST = "http://localhost:8080";


  private static final String GROBID_QUANTITY_NER_PATH = "/processQuantityText";

  private String restHostUrlStr;

  public GrobidQuantityRecogniser() {
    String restHostUrlStr = null;
    try {
      restHostUrlStr = readRestUrl();
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (restHostUrlStr == null
        || (restHostUrlStr != null && restHostUrlStr.equals(""))) {
      this.restHostUrlStr = GROBID_REST_HOST;
    } else {
      this.restHostUrlStr = restHostUrlStr;
    }
  }

  public void recognise(String text)  {

	try{
		text=URLEncoder.encode(text,"UTF-8");
		text=text.replace("%C2%A0", "+");
		System.out.println(text);
		Response response = WebClient
				.create(restHostUrlStr + GROBID_QUANTITY_NER_PATH+ "?text=" + text)
				.accept(MediaType.APPLICATION_JSON)
				.get();
		//int responseCode = response.getStatus();
		String resp = response.readEntity(String.class);
    	System.out.println(resp.toString());
//        if (responseCode == 200) {
//        	String resp = response.readEntity(String.class);
//        	System.out.println(resp.toString());
//        }
//        else
//        {
//        	System.out.println("GROBID-QUANTITY REST SERVICE NOT WORKING!");
//        }
    } 
	catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static String readRestUrl() throws IOException {
    Properties grobidProperties = new Properties();
    grobidProperties.load(GrobidQuantityRecogniser.class
        .getResourceAsStream("GrobidExtractor.properties"));
    return grobidProperties.getProperty("grobid.server.url");
  }

  
//  public static void main(String args[]) throws IOException, InterruptedException
//  {
//	 GrobidQuantityRecogniser obj = new GrobidQuantityRecogniser();
//	 obj.recognise("A 20 kg time");	  
//  }
}
