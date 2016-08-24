package br.ufpe.cin.srmqnlp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.sparse.LinkedSparseMatrix;

public class TextOccurencesMatrix {
	private Map<String, Integer> docToIndices;
	private Matrix mat;
	
	/**
	 * 
	 * @param vocabSize the number of words in the vocabulary. Indices are assumed to be from 1 to <code>vocabSize</code>
	 */
	public TextOccurencesMatrix(File basePath, int vocabSize) throws IOException {
		this.docToIndices = new HashMap<String, Integer>();
		computeMatrix(basePath, vocabSize);
	}

	/**
	 * 
	 * @param basePath indices text files are in this path (or subdirs of it)
	 * @param vocabSize the number of words in the vocabulary. Indices are assumed to be from 1 to <code>vocabSize</code>
	 * @return
	 * @throws IOException 
	 */
	private void computeMatrix(File basePath, int vocabSize) throws IOException {
		if(!basePath.isDirectory() || !basePath.canRead()) {
			throw new IllegalArgumentException("basePath should be a readable directory");
		}
		List<File> filesToProcess = new LinkedList<File>();
		findFilesToProcess(basePath, vocabSize, filesToProcess);
		Collections.shuffle(filesToProcess, new Random(1));
		this.mat = new LinkedSparseMatrix(vocabSize, filesToProcess.size());
		{
			int col = 0;
			for (File f : filesToProcess) {
				processFile(f, mat, col);
				col++;
			}
		}
	}
	
	public void printOccurencesMatrix(OutputStream os) {
        // Output into coordinate format. Indices start from 1 instead of 0
        try {
			Formatter out = new Formatter(os, "UTF-8", Locale.US);
	        out.format("%10d %10d %19d\n", this.mat.numRows(), this.mat.numColumns(),
	                Matrices.cardinality(this.mat));

	        for (MatrixEntry e : this.mat) {
	            if (e.get() != 0)
	                out.format("%10d %10d % .12e\n", e.row() + 1, e.column() + 1,
	                        e.get());
	        }
	        out.flush();
			out.close();
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}

	}
	
	private void processFile(File f, Matrix mat, int col) throws IOException {
		this.docToIndices.put(f.getPath(), col);
		BufferedReader bufR = new BufferedReader(new FileReader(f));
		String line;
		while ((line = bufR.readLine()) != null) {
			int index = Integer.parseInt(line.trim());
			index--;
			mat.add(index, col, 1.0);
		}
		bufR.close();
		
	}

	private void findFilesToProcess(File fileOrDirToProcess, int vocabSize, List<File> filesToProcess) throws IOException {
		if (fileOrDirToProcess.isDirectory()) {
			File[] subFiles = fileOrDirToProcess.listFiles();
			for (File file : subFiles) {
				findFilesToProcess(file, vocabSize, filesToProcess);
			}
		} else {
			filesToProcess.add(fileOrDirToProcess);
		}
	}
	public static void main(String[] args) throws IOException{
		final String basePath = "/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-noheaders-indices-sample10";
		final String outFile = "/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-noheaders-sample10-occurMatrix.txt";
		int vocabSize;
		{
			final String vocab = "/home/srmq/devel/senna/hash/words.lst";
			BufferedReader bufR = new BufferedReader(new FileReader(vocab));
			vocabSize = 0;
			while(bufR.readLine() != null) vocabSize++;
			bufR.close();
		}
		System.out.println("Vocabulary size is " + vocabSize);
		TextOccurencesMatrix textMatrix = new TextOccurencesMatrix(new File(basePath), vocabSize);
		BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(outFile));
		textMatrix.printOccurencesMatrix(outStream);
		outStream.close();
		/*
		 * Exemplo lendo no R como data.frame: 
		 * 
		 * newsgroupData <- read.table("/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-noheaders-sample10-occurMatrix.txt", header=FALSE, colClasses=c("integer", "integer", "numeric"), col.names=c("row", "col", "val"), skip=1)
		 * 
		 */
	}
}
