package br.ufpe.cin.srmqnlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
/**
 * 
 * The first line is the number of documents in the corpus.
 * Then, the n-th line is the number of documents that contain the word with id n-1
 * @author srmq
 *
 */
public class DFWriter {

	public static void main(String[] args)  throws IOException  {
		if (args.length != 1) {
			System.err.println("Should provide 1 argument <baseInputPath>");
			System.exit(-1);
		}
		File baseInputPath = new File(args[0]);
		if (!baseInputPath.isDirectory()) {
			System.err.println("baseInputPath should be a directory");
			System.exit(-2);
		}
		if (!baseInputPath.canRead()) {
			System.err.println("Application does not have read permissions to baseInputPath");
			System.exit(-4);
		}
		Map<Integer, Integer> indexToOcurrenceCount = new HashMap<Integer, Integer>();
		int filesSeen = recursiveProcess(baseInputPath, indexToOcurrenceCount);
		final int maxIndex = Collections.max(indexToOcurrenceCount.keySet());
		System.out.println(filesSeen);
		for (int i = 1; i <= maxIndex; i++) {
			if (indexToOcurrenceCount.containsKey(i)) {
				System.out.println(indexToOcurrenceCount.get(i));
			} else {
				System.out.println("0");
			}
		}
	}

	private static int recursiveProcess(File baseInputPath, Map<Integer, Integer> indexToOcurrenceCount) throws IOException {
		int filesSeen = 0;
		File[] stuffToProcess = baseInputPath.listFiles();
		for (File file : stuffToProcess) {
			if (file.isDirectory()) {
				filesSeen += recursiveProcess(file, indexToOcurrenceCount);
			} else if (file.isFile()){
				++filesSeen;
				BufferedReader bufr = new BufferedReader(new FileReader(file));
				String line;
				while ((line = bufr.readLine()) != null) {
					int index = Integer.parseInt(line);
					if (indexToOcurrenceCount.containsKey(index)) {
						final int count = indexToOcurrenceCount.get(index);
						indexToOcurrenceCount.put(index, count+1);
					} else {
						indexToOcurrenceCount.put(index, 1);
					}
				}
				bufr.close();
			} else {
				throw new IllegalStateException("Found something that is not a file nor a directory");
			}
		}
		return filesSeen;
	}

}
