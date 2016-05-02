package cbor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.gson.Gson;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.DataItem;

public class CborReader {
	
	/**
	 * Read CBOR document from file
	 * @param file
	 * @return parsed CborDocument
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws CborException
	 */
	public static CborDocument read(File file) throws FileNotFoundException, IOException, CborException {
		try (FileInputStream fis = new FileInputStream(file)) {
			return read(file.getName(), fis);
		}
	}
	
	/**
	 * Read CBOR document from path
	 * @param path
	 * @return parsed CborDocument
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws CborException
	 */
	public static CborDocument read(String path) throws FileNotFoundException, IOException, CborException {
		return read(new File(path));
	}
	
	/**
	 * Read CBOR document from input stream
	 * @param fileName
	 * @param fis
	 * @return parsed CborDocument
	 * @throws CborException
	 */
	public static CborDocument read(String fileName, FileInputStream fis) throws CborException {
		CborDecoder decoder = new CborDecoder(fis);
		DataItem dataItem = decoder.decode().get(0);
		
		Gson gson = new Gson();
		CborDocument doc = gson.fromJson(dataItem.toString(), CborDocument.class);
		doc.setFileName(fileName);
		return doc;
	}
	
}
