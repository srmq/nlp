package br.ufpe.cin.srmqnlp;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Hello world!
 *
 */
public class CWEmbeddingWriter extends WordsToIndexBaseProcessor
{
	public static final String SENNA_PATH = "/home/srmq/devel/senna"; 
	public static final String CW_EMBEDDINGS = SENNA_PATH + "/embeddings/embeddings.txt";
	public static final String CW_WORDS = SENNA_PATH + "/hash/words.lst";
	
	public static final int UNK_WORD_ID = 2;
	
	private static final Locale locale = Locale.ENGLISH;
	
	public CWEmbeddingWriter() throws IOException {
		super(new File(CW_WORDS), UNK_WORD_ID, locale);
	}
}
