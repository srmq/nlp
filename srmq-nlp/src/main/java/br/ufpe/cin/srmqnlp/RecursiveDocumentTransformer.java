package br.ufpe.cin.srmqnlp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import br.cin.ufpe.nlp.api.transform.DocumentProcessor;
import br.cin.ufpe.nlp.util.RecursiveTransformer;

public class RecursiveDocumentTransformer {

	/**
	 * 
	 * @param baseInputPath
	 * @param baseOutputPath
	 * @throws IOException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (args.length < 3 || args.length > 4) {
			System.err.println("Should provide 3 arguments <baseInputPath> and <baseOutputPath> <docprocessorClassName> OR 4 arguments <baseInputPath> <baseOutputPath> <docprocessorClassName> <keep_percent>");
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
		if (!baseOutputPath.canWrite()) {
			System.err.println("Application does not have write permissions to baseOutputPath");
			System.exit(-5);
		}
		double keepPercent = 1.0;
		if (args.length == 4) {
			keepPercent = Double.parseDouble(args[3]);
		}
		
		DocumentProcessor docProcessor = Class.forName(args[2]).asSubclass(DocumentProcessor.class).newInstance();

		RecursiveTransformer.recursiveProcess(baseInputPath, baseOutputPath, docProcessor, keepPercent);

	}


}
