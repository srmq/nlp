package br.ufpe.cin.srmqnlp.datasets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WebKBDataPreparator {

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Should give two pathnames as arguments: input root directory (baseInputPath), output root directory (baseOutputPath)");
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
		WebKBDataPreparator dataPrep = new WebKBDataPreparator();
		dataPrep.processFiles(baseInputPath, baseOutputPath);
		

	}
	
	private void processFiles(File baseInputPath, File baseOutputPath) throws IOException {
		final int[] indexDoc = new int[]{1};
		File[] stuffToProcess = baseInputPath.listFiles();
		for (File file : stuffToProcess) {
			final String name = file.getName();
			if (file.isDirectory()) {
				final File outputPathCluster = new File(baseOutputPath.getCanonicalFile().toString() + File.separator + name);
				outputPathCluster.mkdir();
				recursiveProcess(file, outputPathCluster, indexDoc); 
			} else {
				System.err.println("Warning: file " + file.getName() + "in baseInputPath was ignored");
			}
			
		}
		System.out.println("Processed " + (indexDoc[0]-1) + " files");
	}
	
	private void recursiveProcess(File fileOrDirToProcess, File outputPathCluster, int[] index) throws IOException {
		if (fileOrDirToProcess.isDirectory()) {
			File[] subFiles = fileOrDirToProcess.listFiles();
			for (File file : subFiles) {
				recursiveProcess(file, outputPathCluster, index);
			}
		} else {
			BufferedWriter bufw = new BufferedWriter(new FileWriter(new File(outputPathCluster.getCanonicalPath() + File.separator + (index[0]++) + "-" + fileOrDirToProcess.getName() + ".txt")));
			Document doc = Jsoup.parse(fileOrDirToProcess, "UTF-8");
			String contents = doc.body().text();
			bufw.write(contents);
			bufw.close();
		}
	}

}
