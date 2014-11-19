package br.ufpe.cin.srmqnlp;

import java.io.File;
import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
	public static final String SENNA_PATH = "C:/Users/Sergio/Dropbox/CIn/research/textmining/devel/senna"; 
	public static final String CW_EMBEDDINGS = SENNA_PATH + "/embeddings/embeddings.txt";
	public static final String CW_WORDS = SENNA_PATH + "/hash/words.lst";
	
	
    public static void main( String[] args ) throws IOException
    {
    	Vocabulary vocab = new Vocabulary(new File(CW_WORDS));
    	
        System.out.println( "Hello World!" );
    }
    
    
}
