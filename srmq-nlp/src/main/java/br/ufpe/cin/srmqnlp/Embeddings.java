package br.ufpe.cin.srmqnlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import br.cin.ufpe.nlp.util.Vocabulary;

public class Embeddings {
	private Vocabulary vocab;
	private float[][] embeds;
	private final int vecSize;
	public Embeddings(File embFile, Vocabulary vocab, int vecSize) throws IOException{
		this.vocab = vocab;
		final int vocabSize = vocab.size();
		this.vecSize = vecSize;
		this.embeds = new float[vocabSize][];
		BufferedReader bufw = new BufferedReader(new FileReader(embFile));
		for (int i = 0; i < vocabSize; i++) {
			final String line = bufw.readLine();
			this.embeds[i] = new float[vecSize];
			final StringTokenizer strTok = new StringTokenizer(line);
			for (int j = 0; j < vecSize; j++) {
				final String nextToken = strTok.nextToken();
				final float value = Float.parseFloat(nextToken);
				this.embeds[i][j] = value;
			}
		}
		bufw.close();
	}
	
	public float[] embeddingFor(String word) {
		float[] result = null;
		final Integer id = this.vocab.getId(word);
		if(id != null) {
			result = this.embeds[id - 1];
		}
		return result;
	}
	
	public float[] embeddingFor(int id) {
		return this.embeds[id - 1];
	}
	
	public int vecSize() {
		return vecSize;
	}
}
