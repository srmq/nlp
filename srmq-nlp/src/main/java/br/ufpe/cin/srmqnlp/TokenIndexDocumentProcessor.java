package br.ufpe.cin.srmqnlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TokenIndexDocumentProcessor {
	private int unknownWordId;
	public TokenIndexDocumentProcessor(int UNKNOWN_WORD_ID) {
		this.unknownWordId = UNKNOWN_WORD_ID;
	}
	
	public double[][] toEmbeddings(File tokenIndexDocument, Embeddings embed, EnStopWords stopWords) throws IOException {
		int numberOfTokens = countValidTokens(tokenIndexDocument, stopWords);
		double[][] result = new double[numberOfTokens][embed.vecSize()];
		
		BufferedReader buf = new BufferedReader(new FileReader(tokenIndexDocument));
		try {
			String line;
			int i = 0;
			while ((line = buf.readLine()) != null) {
				int id = Integer.parseInt(line);
				if (id != unknownWordId) {
					if (stopWords != null) {
						if (!stopWords.isStopWordIndex(id)) {
							fillEmbedRow(i, result, embed, id);
							++i;
						}
					} else {
						fillEmbedRow(i, result, embed, id);
						++i;
					}
				}
			}
			
		}finally {
			buf.close();
		}
		return result;
	}

	private void fillEmbedRow(int row, double[][] result, Embeddings embed,
			int id) {
		float[] embeds = embed.embeddingFor(id);
		for (int k = 0; k < embeds.length; k++) {
			result[row][k] = embeds[k];
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
