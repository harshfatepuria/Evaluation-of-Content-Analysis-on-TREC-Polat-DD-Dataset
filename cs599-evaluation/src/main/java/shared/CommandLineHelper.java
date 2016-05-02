package shared;

public class CommandLineHelper {
	public static String getArg(String[] args, int index, String defaultVal) {
		if (args.length > index) {
			return args[index];
		}
		
		return defaultVal;
	}
	
	public static String getArg(String[] args, int index) {
		return getArg(args, index, null);
	}
}
