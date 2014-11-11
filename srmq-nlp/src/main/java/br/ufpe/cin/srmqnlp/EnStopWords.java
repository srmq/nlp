package br.ufpe.cin.srmqnlp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EnStopWords {
	public static final String[] EN_STOP_WORDS = { 
		"a", "an", "and", "are", "as", "at", "be", "but", "by", 
		"for", "if", "in", "into", "is", "it", 
		"no", "not", "of", "on", "or", "such", 
		"that", "the", "their", "then", "there", "these", 
		"they", "this", "to", "was", "will", "with"
		};
	
	private Set<String> stopWordSet;
	public EnStopWords() {
		this.stopWordSet = new HashSet<String>(EN_STOP_WORDS.length);
		this.stopWordSet.addAll(Arrays.asList(EN_STOP_WORDS));
	}
}
