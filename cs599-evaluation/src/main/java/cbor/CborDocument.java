package cbor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;

public class CborDocument {
	String url;
	Response response;
	String key;
	String imported;
	String fileName;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("url=" + url + "\n");
		sb.append("body=\n");
		sb.append(response.body);
		
		return sb.toString();
	}
	
	public InputStream getInputStream() throws UnsupportedEncodingException {
		InputStream stream = new ByteArrayInputStream(response.getBodyAsBytes());
		return stream;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fname) {
		this.fileName = fname;
	}
	
	public String getRelativePath() {
		String[] tokens = key.split("_");
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < tokens.length - 1; i++) {
			sb.append(tokens[i]);
			sb.append("/");
		}
		
		sb.append(getFileName());
		
		return sb.toString();
	}
	
	public MediaType getMediaType() {
		return response.getNormalizedMediaType();
	}
	
	public int getFileSize() {
		return response.getFileSize();
	}
	
	public int getBodyLength() {
		return response.body.length();
	}
	
	public String getContentLength() {
		return response.getContentLength();
	}
	
	public String getStatus() {
		return response.status;
	}
	
	public String getUrl() {
		return url;
	}
}

class Response {
	List<String[]> headers;
	String body;
	String status;
	
	static MediaTypeRegistry registry = MediaTypeRegistry.getDefaultRegistry();
	
	public String getHeader(String key) {
		for (String[] s : headers) {
			if (key.equalsIgnoreCase(s[0])) {
				return s[1];
			}
		}
		
		return null;
	}
	
	public String getContentType() {
		return getHeader("Content-Type");
	}
	
	public String getContentLength() {
		return getHeader("Content-Length");
	}
	
	public MediaType getNormalizedMediaType() {
		MediaType type = MediaType.parse(getContentType());
		return registry.normalize(type);
	}
	
	public int getFileSize() {
		return getBodyAsBytes().length;
	}
	
	public byte[] getBodyAsBytes() {
		MediaType type = getNormalizedMediaType();
		String rBody = prepareString(body);
		
		if(type == null) {
			return rBody.getBytes();
		}
		
		try {
			if(type.getParameters().containsKey("charset")) {
				String charset = type.getParameters().get("charset");
				return rBody.getBytes(charset);
			}
			return rBody.getBytes();
		} catch (UnsupportedEncodingException e) {
			return rBody.getBytes();
		}
	}
	
	private String prepareString(String b) {
		return b;
//		return StringEscapeUtils.unescapeJava(b);
		
		/*
		Properties p = new Properties();
		try {
			p.load(new StringReader("key="+b));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return p.getProperty("key");
		*/
	}
}