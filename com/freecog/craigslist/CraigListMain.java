package com.freecog.craigslist;

import java.io.File;

public class CraigListMain {
	
	public static final String USAGE = "Usage: java -jar craigslistmain.jar " +
											"<directory> <search term>";

	public static void main(String[] args) {
		// TODO handle argment checking in a single method
		if (args.length < 2) {
			System.out.println("Error: Check number of arguments.\n");
			System.out.println(USAGE);
			System.exit(0);
		} else if ( checkDirExists(args[0]) ){
			System.out.print("Error ensure directory that results are being " +
								"saved to exists. Directory " + args[0] + " ");
			System.out.println(USAGE);
			System.exit(0);
		} else {
			// TODO handle getting telecommute
			String dir = args[0];
			String[] searchTerms = getArgs(args[1]);
			CraigsList cl = new CraigsList(dir);
			cl.findAllJobsGlobally(searchTerms, true);
		}
	}
	
	public static Boolean checkDirExists(String directory) {
		File dir = new File(directory);
		return dir.exists();
	}
	
	public static String[] getArgs(String argString) {
		String[] args = argString.split(",");
		for(int i = 0; i < args.length; i++) {
			args[i] = args[i].trim();
		}
		
		return args;
	}
	
}
