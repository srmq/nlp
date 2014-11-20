package br.ufpe.cin.srmqnlp;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class RDTWAlignments {
public static void main(String[] args) throws RserveException, REXPMismatchException, IOException {
	RConnection c = new RConnection();
	REXP x = c.eval("R.version.string");
	System.out.println(x.asString());
	TokenIndexDocumentProcessor docProcessor = new TokenIndexDocumentProcessor(CWEmbeddingWriter.UNK_WORD_ID);
	File tokenDocument = new File("/home/srmq/Dropbox/CIn/research/textmining/devel/data/20_newsgroups-noheaders-indices/alt.atheism/49960");
	Vocabulary vocab = new Vocabulary(new File("/home/srmq/Dropbox/CIn/research/textmining/devel/senna/hash/words.lst"));
	Embeddings embed = new Embeddings(new File("/home/srmq/Dropbox/CIn/research/textmining/devel/senna/embeddings/embeddings.txt"), vocab, 50);
	EnStopWords stopWords = new EnStopWords(vocab);
	
	double[][] result = docProcessor.toEmbeddings(tokenDocument, embed, stopWords);
	REXP embedDocA = REXP.createDoubleMatrix(result);
	c.assign("docA", embedDocA);
	REXP teste = c.eval("length(docA)");
	System.out.println(teste.asString());
	System.out.println("All done");
	 
}
}
