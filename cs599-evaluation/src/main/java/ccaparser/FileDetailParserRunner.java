package ccaparser;

import java.io.File;
import java.util.List;

import shared.CommandLineHelper;

public class FileDetailParserRunner extends CCADetailParserRunner {

	public FileDetailParserRunner(String baseFolder, String resultFolder) throws Exception {
		super(baseFolder, resultFolder);
	}
	
	public FileDetailParserRunner(String baseFolder, String resultFolder, String markerFile) throws Exception {
		super(baseFolder, resultFolder, markerFile);
	}

	@Override
	protected File getResultFile(String relativePath) {
		return super.getResultFile(relativePath, ".meta");
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("Run FileDetailParser");
		
		String baseFolder = CommandLineHelper.getArg(args, 0, "C:\\cs599\\polar-fulldump");
		String resultFolder = CommandLineHelper.getArg(args, 1, "C:\\cs599\\a3\\metadata\\result");
		String markerFile = CommandLineHelper.getArg(args, 2, "C:\\cs599\\a3\\metadata\\marker.txt");
		
		FileDetailParserRunner runner = new FileDetailParserRunner(baseFolder, resultFolder, markerFile);
		List<String> successPath = runner.runParser();
		System.out.println("No of files: " + successPath.size());
	}
	
}
