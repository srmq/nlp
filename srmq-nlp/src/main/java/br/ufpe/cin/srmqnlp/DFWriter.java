package br.ufpe.cin.srmqnlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * 
 * The first line is the number of documents in the corpus.
 * Then, the n-th line is the number of documents that contain the word with id n-1
 * @author srmq
 *
 */
public class DFWriter {
	
	private static void findFilesToProcess(File fileOrDirToProcess, List<File> filesToProcess) throws IOException {
		if (fileOrDirToProcess.isDirectory()) {
			File[] subFiles = fileOrDirToProcess.listFiles();
			for (File file : subFiles) {
				findFilesToProcess(file, filesToProcess);
			}
		} else {
			filesToProcess.add(fileOrDirToProcess);
		}
	}
	

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
		Map<Integer, Integer> indexToDF = new HashMap<Integer, Integer>();
		int filesSeen = 0;

		List<File> filesToProcess = new LinkedList<File>();
		findFilesToProcess(baseInputPath, filesToProcess);
		System.out.println("THE FILE LIST SIZE IS " + filesToProcess.size());
		for (File file : filesToProcess) {
			filesSeen++;
			Set<Integer> ids = new HashSet<Integer>();
			BufferedReader bufr = new BufferedReader(new FileReader(file));
			String line;
			while ((line = bufr.readLine()) != null) {
				int index = Integer.parseInt(line);
				ids.add(index);
			}
			for (Integer index : ids) {
				if (indexToDF.containsKey(index)) {
					final int docCount = indexToDF.get(index);
					indexToDF.put(index, docCount+1);
				} else {
					indexToDF.put(index, 1);
				}
			}

			bufr.close();
		}
		final int maxIndex = Collections.max(indexToDF.keySet());
		System.out.println(filesSeen);
		for (int i = 1; i <= maxIndex; i++) {
			if (indexToDF.containsKey(i)) {
				System.out.println(indexToDF.get(i));
			} else {
				System.out.println("0");
			}
		}
	}
}
