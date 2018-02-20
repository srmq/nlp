package br.ufpe.cin.srmqnlp;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;

import br.cin.ufpe.nlp.api.transform.DocumentProcessor;
import br.cin.ufpe.nlp.util.Vocabulary;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.PTBTokenizer.PTBTokenizerFactory;

public class WordsToIndexBaseProcessor implements DocumentProcessor {
	
	protected File wordListFile;
	protected int unknownWordId; 
	protected Vocabulary vocab;
	protected Locale locale;
	

	public WordsToIndexBaseProcessor(File wordListFile, int unkWordId, Locale myLocale) throws IOException {
		this.wordListFile = wordListFile;
		this.unknownWordId = unkWordId;
		this.locale = myLocale;
    	this.vocab = new Vocabulary(wordListFile);
	}

	@Override
	public void processDocument(Reader inputDocument, Writer outputIndices) throws IOException {
    	for (Tokenizer<CoreLabel> tokenizer = PTBTokenizerFactory.newCoreLabelTokenizerFactory("").getTokenizer(inputDocument);
    			tokenizer.hasNext();) {
    		CoreLabel token = tokenizer.next();
    		String tokenString = token.toString().toLowerCase(locale);
    		Integer id = this.vocab.getId(tokenString);
    		if (id == null) {
    			id = unknownWordId;
    		}
    		outputIndices.write(id.toString());
    		outputIndices.write('\n');
    	}
    	outputIndices.flush();

	}

}
