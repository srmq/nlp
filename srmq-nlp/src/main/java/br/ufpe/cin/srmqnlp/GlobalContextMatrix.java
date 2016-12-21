package br.ufpe.cin.srmqnlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import no.uib.cipr.matrix.Matrix;

public class GlobalContextMatrix extends TextFilesToMatrix {
	
	public GlobalContextMatrix(File basePath, int vocabSize) throws IOException {
		super(basePath, vocabSize);
	}

	@Override
	protected void processFile(File f, Matrix mat, int col) throws IOException {
		try(BufferedReader bufReader = new BufferedReader(new FileReader(f))) {
			String line = bufReader.readLine();
			final String[] split = line.split(" ");
			final int splitLen = split.length;
			if (splitLen != this.vocabSize) {
				throw new IllegalArgumentException("File " + f.getPath() + " does not have a embedding of size " + this.vocabSize);
			}
			for (int i = 0; i < splitLen; i++) {
				final double val = Double.parseDouble(split[i]);
				mat.set(i, col, val);
			}
		}

	}

	public static void main(String[] args) throws IOException {
		/**
		 * Reuters RT10-Glove
		 */ /*
		 final String basePath = "/home/srmq/Documents/Research/textmining/devel/data/reuters21578/ModApteTestWithBodySingleTopic/globalContextsGlove/top10Categories";
		 final String outFile = "/home/srmq/Documents/Research/textmining/devel/data/reuters21578/ModApteTestWithBodySingleTopic/globalContextsGlove/reuters21578-RT10-Glove-GCMatrix.txt";
		 final String elementListFile = "/home/srmq/Documents/Research/textmining/devel/data/reuters21578/ModApteTestWithBodySingleTopic/globalContextsGlove/reuters21578-RT10-Glove-GCMatrix-Elements.txt";
		 */

		 /**
		  * 20newsgroups-sample10 Glove
		  */ /*
		 final String basePath = "/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-noheaders-gloveglobalcontext";
		 final String outFile = "/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-sample10-Glove-GCMatrix.txt";
		 final String elementListFile = "/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-sample10-Glove-GCMatrix.txt-Elements.txt";
		 */
		
		 /**
		  * 20newsgroups-sample10 GoogleNews vectors
		  */ /*		
		 final String basePath = "/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-noheaders-googlenewsVectors";
		 final String outFile = "/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-sample10-GoogleNewsVectors-Matrix.txt";
		 final String elementListFile = "/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-sample10-GoogleNewsVectors-Matrix-Elements.txt";
		 */
		
		/**
		 * 20newsgroups-sample Mikolov RNN 1600 vectors
		 */
		 final String basePath = "/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-noheaders-MikolovRNN1600Vec";
		 final String outFile = "/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-sample10-MikolovRNN1600Vec-Matrix.txt";
		 final String elementListFile = "/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-sample10-MikolovRNN1600Vec-Matrix-Elements.txt";
		 final int embedSize = 1600;
		
		
		 //final int embedSize = 300;
		 GlobalContextMatrix gcMatrix = new GlobalContextMatrix(new File(basePath), embedSize);
		 gcMatrix.printOutputMatrix(outFile);
		 gcMatrix.printElementList(elementListFile);

	}

}
