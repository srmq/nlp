package br.ufpe.cin.srmqnlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenIndexDocumentProcessor {
	private int unknownWordId;
	
	public TokenIndexDocumentProcessor(int UNKNOWN_WORD_ID) {
		this.unknownWordId = UNKNOWN_WORD_ID;
	}
	
	public class DFMapping {
		private Map<Integer, Integer> dfOfIndices;
		private int nDocs;
		private DFMapping(File dfFile) throws IOException {
			dfOfIndices = new HashMap<Integer, Integer>();
			BufferedReader buf = null;
			try {
				buf = new BufferedReader(new FileReader(dfFile));
				String line = buf.readLine();
				nDocs = Integer.parseInt(line);
				if (Constants._DEBUG) {
					if (nDocs <= 0) throw new IllegalStateException("df file has invalid number of documents");
				}
				
				for (int index = 1; (line = buf.readLine()) != null; index++) {
					final int dfForIndex = Integer.parseInt(line);
					if (Constants._DEBUG) {
						if (dfForIndex < 0) throw new IllegalStateException("df file has negative df value at line " + (index+1));
						else if (dfForIndex > nDocs) throw new IllegalStateException("df file has df value bigger than number of documents at line " + (index+1));
					}
					if (dfForIndex != 0) {
						dfOfIndices.put(index, dfForIndex);
					}
				}
			} finally {
				if (buf != null) buf.close();
			}
		}
		public int getnDocs() {
			return nDocs;
		}
		public int dfOfIndex(int index) {
			if (dfOfIndices.containsKey(index)) return dfOfIndices.get(index);
			else return 0;
		}
	}
	
	public DFMapping generateDFMapping(File dfFile) throws IOException{
		return new DFMapping(dfFile);
	}
	
	public double[][] toEmbeddings(File tokenIndexDocument, Embeddings embed, EnStopWords stopWords) throws IOException {
		return toEmbeddings(tokenIndexDocument, embed, stopWords, null);
	}
	
	public double[][] toEmbeddings(File tokenIndexDocument, Embeddings embed, EnStopWords stopWords, DFMapping df) throws IOException {
		//FIXME testar geracao de tf idf
		final boolean genTfIdf = (df != null);
		int numberOfTokens = countValidTokens(tokenIndexDocument, stopWords);
		int vecSize = embed.vecSize();
		if (genTfIdf) vecSize += 2;
		double[][] result = new double[numberOfTokens][vecSize];
		
		BufferedReader buf = new BufferedReader(new FileReader(tokenIndexDocument));
		try {
			String line;
			int i = 0;
			Map<Integer, Double> tf = null;
			List<Integer> tokens = null;
			if (genTfIdf) { 
				tf = new HashMap<Integer, Double>();
				tokens = new ArrayList<Integer>();
			}
			while ((line = buf.readLine()) != null) {
				int id = Integer.parseInt(line);
				if (id != unknownWordId) {
					if (stopWords != null) {
						if (!stopWords.isStopWordIndex(id)) {
							fillEmbedRow(i, result, embed, id, genTfIdf);
							if (genTfIdf) {
								double freqId = 0.0;
								if (tf.containsKey(id)) {
									freqId = tf.get(id);
								}
								tf.put(id, freqId + 1.0);
								tokens.add(id);
							}
							++i;
						}
					} else {
						fillEmbedRow(i, result, embed, id, genTfIdf);
						if (genTfIdf) {
							double freqId = 0.0;
							if (tf.containsKey(id)) {
								freqId = tf.get(id);
							}
							tf.put(id, freqId + 1.0);
							tokens.add(id);
						}

						++i;
					}
				}
			}
			if (genTfIdf) {
				for (int tokenI = 0; tokenI < numberOfTokens; tokenI++) {
					double tokenTf = tf.get(tokens.get(tokenI));
					double tokenIdf = Math.log((double)df.getnDocs()/(double)df.dfOfIndex(tokens.get(tokenI)));
					result[tokenI][0] = tokenTf;
					result[tokenI][1] = tokenIdf;
				}
			}
			
		}finally {
			buf.close();
		}
		return result;
	}

	private void fillEmbedRow(int row, double[][] result, Embeddings embed,
			int id, boolean genTfIdf) {
		float[] embeds = embed.embeddingFor(id);
		for (int k = 0; k < embeds.length; k++) {
			if (!genTfIdf) {
				result[row][k] = embeds[k];
			} else {
				result[row][k+2] = embeds[k];
			}
		}
		
	}

	private int countValidTokens(File tokenIndexDocument, EnStopWords stopWords) throws IOException {
		BufferedReader buf = new BufferedReader(new FileReader(tokenIndexDocument));
		int result = 0;
		try {
			String line;
			while ((line = buf.readLine()) != null) {
				int id = Integer.parseInt(line);
				if (id != unknownWordId) {
					if (stopWords != null) {
						if (!stopWords.isStopWordIndex(id)) {
							++result;
						}
					} else {
						++result;
					}
				}
			}
		} finally {
			buf.close();
		}
		return result;
	}
}
