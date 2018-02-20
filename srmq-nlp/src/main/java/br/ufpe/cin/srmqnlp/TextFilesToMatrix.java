package br.ufpe.cin.srmqnlp;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.sparse.LinkedSparseMatrix;

public abstract class TextFilesToMatrix {
	protected Map<String, Integer> docToIndices;
	private Matrix mat;
	List<String> elements = new LinkedList<String>();
	
	protected int vocabSize;
	
	private File basePath;
	
	public TextFilesToMatrix(File basePath, int vocabSize) throws IOException {
		this(basePath, vocabSize, false);
	}
	
	/**
	 * 
	 * @param vocabSize the number of words in the vocabulary. Indices are assumed to be from 1 to <code>vocabSize</code>
	 */
	protected TextFilesToMatrix(File basePath, int vocabSize, boolean lazy) throws IOException {
		this.basePath = basePath;
		this.vocabSize = vocabSize;
		this.docToIndices = new HashMap<String, Integer>();
		if(!lazy) {
			computeMatrix();
		}
	}

	public void computeMatrix() throws IOException {
		if(!basePath.isDirectory() || !basePath.canRead()) {
			throw new IllegalArgumentException("basePath should be a readable directory");
		}
		List<File> filesToProcess = new LinkedList<File>();
		findFilesToProcess(basePath, filesToProcess);
		System.out.println("THE FILELIST TO PROCESS HAS SIZE " + filesToProcess.size());
		Collections.shuffle(filesToProcess, new Random(1));
		SortedSet<String> groupNames = new TreeSet<String>();
		List<String> fileGroups = new ArrayList<String>(filesToProcess.size());
		this.mat = Matrices.synchronizedMatrix(new LinkedSparseMatrix(vocabSize, filesToProcess.size()));
		final Matrix matToProcess = this.mat;
		{
			ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			try {
				int col = 0;
				for (final File f : filesToProcess) {
					final int currCol = col;
					final String fileGroupName = extractGroupName(f, groupNames);
					fileGroups.add(fileGroupName);
					this.docToIndices.put(f.getPath(), col);
					exec.submit(new Runnable() {
						@Override
						public void run() {
							try {
								System.out.println("Processing column " + currCol);
								processFile(f, matToProcess, currCol);
							} catch (IOException e) {
								throw new IllegalStateException("Error processing file " + f.getName(), e);
							}						
						}
					});
					col++;
				}
			} finally {
				exec.shutdown();
				try {
					exec.awaitTermination(100, TimeUnit.DAYS);
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new IllegalStateException("Could not process files", e);
				}
			}
			
		}
		Map<String, Integer> groupIndices = new HashMap<String, Integer>(groupNames.size());
		{
			int i = 0;
			for (Iterator<String> iterator = groupNames.iterator(); iterator.hasNext(); i++) {
				String groupName = (String) iterator.next();
				groupIndices.put(groupName, i);
			}
		}
		{
			Iterator<String> fileGroupsIt = fileGroups.iterator();
			Iterator<File> fileIterator = filesToProcess.iterator();
			while (fileIterator.hasNext()) {
				assert(fileGroupsIt.hasNext());
				StringBuilder stb = new StringBuilder();
				String groupName = fileGroupsIt.next();
				String fileName = fileIterator.next().getName();
				stb.append(groupIndices.get(groupName));
				stb.append(",\"");
				stb.append(groupName);
				stb.append("/");
				stb.append(fileName.substring(fileName.lastIndexOf(File.separatorChar)+1, fileName.length()));
				stb.append("\"");
				elements.add(stb.toString());
			}
			assert(elements.size() == filesToProcess.size());
		}
	}
	
	private String extractGroupName(File f, SortedSet<String> groupNames) {
		String fullName = f.getAbsolutePath();
		int lastSep = fullName.lastIndexOf(File.separatorChar);
		int beforeLastSep = fullName.substring(0, lastSep).lastIndexOf(File.separatorChar);
		String groupName = fullName.substring(beforeLastSep+1, lastSep);
		groupNames.add(groupName);
		return groupName;
	}

	public void printOutputMatrix(OutputStream os) {
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
	
	/**
	 * Responsible for transforming a file to a column into the output matrix.
	 * @param f the file to be transformed
	 * @param mat the output matrix
	 * @param col the column that should refer to this file
	 * @throws IOException
	 */
	protected abstract void processFile(File f, Matrix mat, int col) throws IOException;

	private void findFilesToProcess(File fileOrDirToProcess, List<File> filesToProcess) throws IOException {
		if (fileOrDirToProcess.isDirectory()) {
			File[] subFiles = fileOrDirToProcess.listFiles();
			for (File file : subFiles) {
				findFilesToProcess(file, filesToProcess);
			}
		} else {
			filesToProcess.add(fileOrDirToProcess);
		}
	}
	
	public void printOutputMatrix(final String outFile)
			throws FileNotFoundException, IOException {
		BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(outFile));
		this.printOutputMatrix(outStream);
		outStream.close();
	}
	
	public void printElementList(final String elementListFile)
			throws IOException {
		BufferedWriter outWriter = new BufferedWriter(new FileWriter(elementListFile));
		for (String element : this.elements) {
			outWriter.write(element);
			outWriter.write(System.lineSeparator());
		}
		outWriter.close();
	}
	
	
}
