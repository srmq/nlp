package br.ufpe.cin.srmqnlp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class RecursiveCWIndexWriter {

	/**
	 * 
	 * @param baseInputPath
	 * @param baseOutputPath
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Should provide 2 arguments <baseInputPath> and <baseOutputPath>");
			System.exit(-1);
		}
		File baseInputPath = new File(args[0]);
		File baseOutputPath = new File (args[1]);
		if (!baseInputPath.isDirectory()) {
			System.err.println("baseInputPath should be a directory");
			System.exit(-2);
		}
		if (!baseOutputPath.isDirectory()) {
			System.err.println("baseOutputPath should be a directory");
			System.exit(-3);
		}
		if (!baseInputPath.canRead()) {
			System.err.println("Application does not have read permissions to baseInputPath");
			System.exit(-4);
		}
		if (!baseInputPath.canWrite()) {
			System.err.println("Application does not have write permissions to baseOutputPath");
			System.exit(-5);
		}
		CWEmbeddingWriter cwWriter = new CWEmbeddingWriter();
		recursiveProcess(baseInputPath, baseOutputPath, cwWriter);

	}

	private static void recursiveProcess(File baseInputPath, File baseOutputPath, CWEmbeddingWriter cwWriter) throws IOException {
		File[] stuffToProcess = baseInputPath.listFiles();
		for (File file : stuffToProcess) {
			final String name = file.getName();
			if (file.isDirectory()) {
				final File newDir = new File(baseOutputPath.getCanonicalFile().toString() + File.separator + name);
				newDir.mkdir();
				recursiveProcess(file, newDir, cwWriter); 
			} else {
				BufferedReader bufr = new BufferedReader(new FileReader(file));
				BufferedWriter bufw = new BufferedWriter(new FileWriter(new File(baseOutputPath.getCanonicalPath() + File.separator + name)));
				
				bufw.close();
				bufr.close();
			}
		}
		
	}

}
