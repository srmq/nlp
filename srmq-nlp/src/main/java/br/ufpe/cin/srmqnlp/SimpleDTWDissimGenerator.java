package br.ufpe.cin.srmqnlp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

public class SimpleDTWDissimGenerator {
/*
 library("inline")
 src <- '
    Rcpp::NumericVector xa(a);
    Rcpp::NumericVector xb(b);
    int n = xa.size();
    double dist = 0.0;
    for (int i = 0; i < n; i++) {
	const double idist = xa[i] - xb[i];
	dist += idist*idist;
    }
    return Rcpp::wrap(sqrt(dist));
'
fun <- cxxfunction(signature(a = "numeric", b = "numeric"), src, plugin="Rcpp")
 */
	
public static void main(String[] args) throws IOException, REXPMismatchException, REngineException {
	if (args.length < 1 || args.length > 2) {
		System.err.println("Should give parent directory of files as argument [and distance function default: Euclidean]");
		System.exit(-1);
	}
	File parentDir = new File(args[0]);
	if (!parentDir.isDirectory()) {
		System.err.println("Argument given is not a directory");
		System.exit(-2);
	}
	if (!parentDir.canRead()) {
		System.err.println("Cannot read from given directory");
		System.exit(-3);
		
	}
	final String distanceFunction = (args.length == 1) ? "Euclidean" : args[1];
	
	File []subFiles = parentDir.listFiles();
	List<File> clusters = new ArrayList<File>(subFiles.length);
	for (int i = 0; i < subFiles.length; i++) {
		final File f = subFiles[i];
		if (f.isDirectory()) {
			clusters.add(f);
		}
	}
	Collections.sort(clusters);
	List<File> allFiles = new ArrayList<File>();
	for (int c = 0; c < clusters.size(); c++) {
		File []textFiles = clusters.get(c).listFiles();
		Arrays.sort(textFiles);
		for (File file : textFiles) {
			if (file.isFile()) {
				allFiles.add(file);
				System.out.println(c + ",\"" + file.getParentFile().getName() + File.separator + file.getName() + "\"");
			}
		}
	}
	//System.out.println("total files: " + allFiles.size());

	Vocabulary vocab = new Vocabulary(new File(CWEmbeddingWriter.CW_WORDS));
	Embeddings embed = new Embeddings(new File(CWEmbeddingWriter.CW_EMBEDDINGS), vocab, 50);
	EnStopWords stopWords = new EnStopWords(vocab);
	TokenIndexDocumentProcessor docProcessor = new TokenIndexDocumentProcessor(CWEmbeddingWriter.UNK_WORD_ID);
	RConnection rConn = new RConnection();
	rConn.voidEval("library(\"dtw\")");
	byte gcCount = 0;
	for (int me = 0; me < allFiles.size(); me++) {
		double[][] myEmbeddings = docProcessor.toEmbeddings(allFiles.get(me), embed, stopWords);
		REXP embedDoc = REXP.createDoubleMatrix(myEmbeddings);
		rConn.assign("myEmbeds", embedDoc);
		embedDoc = null;
		for (int other = 0; other < me; other++) {
			double[][] otherEmbeddings = docProcessor.toEmbeddings(allFiles.get(other), embed, stopWords);
			final boolean hasZeroLengthDoc = myEmbeddings.length == 0 || otherEmbeddings.length == 0;
			embedDoc = REXP.createDoubleMatrix(otherEmbeddings);
			rConn.assign("otherEmbeds", embedDoc);
			embedDoc = null;
			String myCode;
			double distance = -1.0;
			if (!hasZeroLengthDoc) {
				distance = -1.1;
				if (myEmbeddings.length > otherEmbeddings.length) {
					myCode = "OAlign <- dtw(otherEmbeds, myEmbeds, dist.method=\"" + distanceFunction + "\", step=asymmetric, distance.only=TRUE, open.begin=TRUE, open.end=TRUE)"; 
				} else {
					myCode = "OAlign <- dtw(myEmbeds, otherEmbeds, dist.method=\"" + distanceFunction + "\", step=asymmetric, distance.only=TRUE, open.begin=TRUE, open.end=TRUE)"; 
				}
				final REXP r = rConn.parseAndEval("try("+myCode+",silent=TRUE)");
				if (r.inherits("try-error")) { 
					System.err.println("Error: "+r.asString());
				} else {
					distance = rConn.eval("OAlign$normalizedDistance").asDouble();
				}
			}
			System.out.print(distance + ",");
			if (++gcCount % 100 == 0){
				rConn.voidEval("gc()");
				gcCount = 0;
			}
			rConn.voidEval("rm(otherEmbeds)");
		}
		
		System.out.println("0.0");
		rConn.voidEval("rm(myEmbeds)");
	}
	rConn.close();
} 

}
