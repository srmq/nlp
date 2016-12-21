package br.ufpe.cin.srmqnlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

import com.google.common.base.Optional;

public class SimpleDTWDissimGenerator {
	public static final String RSERVE_CONFIG_FILE = System.getProperty("user.dir") + File.separator + "Rserv.conf";
	private static Map<String, String[]> customDistFunctions;
	// accepted distances: everything R (Euclidian, cosine...); meanWfIdfEuclidian, meanWfIdfCosineDist
	static {
		customDistFunctions = new HashMap<String, String[]>(); // maps from function name to function code
		{ // Custom Euclidian with log(tf)*idf weighting (mean of the two words)
			String meanWfIdfEuclidian = "meanWfIdfEuclidian";
			String[] meanWfIdfEuclidianCode = {"src <- '" +  
												"Rcpp::NumericVector xa(a); " +
												"Rcpp::NumericVector xb(b); " +
												"int n = xa.size(); " +
												"double dist = 0.0; " +
												"for (int i = 2; i < n; i++) { " +
													"const double idist = xa[i] - xb[i]; " +
													"dist += idist*idist; " +
												"} "						   +
												"const double wfdfA = (xa[0] > 0) ? (1.0 + log(xa[0])) : 0.0; " +
												"const double wfdfB = (xb[0] > 0) ? (1.0 + log(xb[0])) : 0.0; " +
												"const double meanWfIdf = ((wfdfA * xa[1]) + (wfdfB * xb[1]))*0.5; " +
												"return Rcpp::wrap(meanWfIdf * sqrt(dist)); " +
												"'",
											"meanWfIdfEuclidian <- cxxfunction(signature(a = \"numeric\", b = \"numeric\"), src, plugin=\"Rcpp\")",
											"pr_DB$set_entry(FUN = meanWfIdfEuclidian, names = c(\"test_meanWfIdfEuclidian\", \"meanWfIdfEuclidian\"))",
											"rm(src)"};
			customDistFunctions.put(meanWfIdfEuclidian, meanWfIdfEuclidianCode);
		}
		
		{ // Custom Cosine dist with log(tf)*idf weighting (mean of the two words)
			String meanWfIdfCosineDist = "meanWfIdfCosineDist";
			String[] meanWfIdfCosineDistCode = {"src <- '" +  
												"Rcpp::NumericVector xa(a); " +
												"Rcpp::NumericVector xb(b); " +
												"int n = xa.size(); " +
												"double sumAiBi = 0.0, aiSq = 0.0, biSq = 0.0; " +
												"for (int i = 2; i < n; i++) { " +
													"sumAiBi += xa[i] * xb[i]; " +
													"aiSq += xa[i] * xa[i]; " +
													"biSq += xb[i] * xb[i]; " +
												"} "						   +
												"const double cosine = sumAiBi / (sqrt(aiSq)*sqrt(biSq)); " +
												"const double cosineDist =  1.0 - (1.0 + cosine)*0.5; " +
												"const double wfdfA = (xa[0] > 0) ? (1.0 + log(xa[0])) : 0.0; " +
												"const double wfdfB = (xb[0] > 0) ? (1.0 + log(xb[0])) : 0.0; " +
												"const double meanWfIdf = ((wfdfA * xa[1]) + (wfdfB * xb[1]))*0.5; " +
												"return Rcpp::wrap(meanWfIdf * cosineDist); " +
												"'",
											"meanWfIdfCosineDist <- cxxfunction(signature(a = \"numeric\", b = \"numeric\"), src, plugin=\"Rcpp\")",
											"pr_DB$set_entry(FUN = meanWfIdfCosineDist, names = c(\"test_meanWfIdfCosineDist\", \"meanWfIdfCosineDist\"))",
											"rm(src)"};
			customDistFunctions.put(meanWfIdfCosineDist, meanWfIdfCosineDistCode);
		}
		
		
	}
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

public static Map<String, String> parseRConfFile(File rServeConfFile) throws IOException {
	final Map<String, String> ret = new HashMap<String, String>();
	try (BufferedReader bufReader = new BufferedReader(new FileReader(rServeConfFile))) {
		String line;
		while ((line = bufReader.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("#"))
				continue;
			final StringTokenizer strTok = new StringTokenizer(line);
			final String key = strTok.nextToken();
			final String value = (strTok.hasMoreTokens()) ? strTok.nextToken() : "";
			ret.put(key, value);
		}
	}
	return ret;
}
	
public static void main(String[] args) throws IOException, REXPMismatchException, REngineException {
	if (args.length < 1 || args.length > 3) {
		System.err.println("Should give parent directory of files as argument [plus distance function default: Euclidean and df file (if custom)]");
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
	
	File rServeConfFile = new File(RSERVE_CONFIG_FILE);
	System.err.println("Rserve conf should be at " + rServeConfFile.getAbsolutePath());
	Optional<Integer> rServePort = Optional.absent();
	if (rServeConfFile.exists()) {
		Map<String, String> rServeOptions = parseRConfFile(rServeConfFile);
		if (rServeOptions.containsKey("port")) {
			rServePort = Optional.of(Integer.parseInt(rServeOptions.get("port")));
		}
	}
	
	
	
	boolean useCustomTfIdf = (args.length == 3);
	
	File dfFile = null;
	if (useCustomTfIdf) dfFile = new File(args[2]);
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
	TokenIndexDocumentProcessor.DFMapping dfMapping = null;
	if (useCustomTfIdf) dfMapping = docProcessor.generateDFMapping(dfFile);
	RConnection rConn;
	if (rServePort.isPresent()) {
		rConn = new RConnection("localhost", rServePort.get());
	} else {
		rConn = new RConnection();
	}
	if (!rConn.isConnected()) {
		throw new IllegalStateException("Could not connect to Rserve");
	}
	rConn.voidEval("library(\"dtw\")");
	if (useCustomTfIdf) { 
		rConn.voidEval("library(\"inline\")");
		if (!customDistFunctions.containsKey(distanceFunction)) {
			throw new IllegalStateException("Custom function " + distanceFunction + " does not exist");
		}
		String[] codeToExecute = customDistFunctions.get(distanceFunction);
		for (String codeLine : codeToExecute) {
			rConn.voidEval(codeLine);
		}
	}
	byte gcCount = 0;
	for (int me = 0; me < allFiles.size(); me++) {
		double[][] myEmbeddings;
		if (!useCustomTfIdf) myEmbeddings = docProcessor.toEmbeddings(allFiles.get(me), embed, stopWords);
		else myEmbeddings = docProcessor.toEmbeddings(allFiles.get(me), embed, stopWords, dfMapping);
		REXP embedDoc = REXP.createDoubleMatrix(myEmbeddings);
		rConn.assign("myEmbeds", embedDoc);
		embedDoc = null;
		for (int other = 0; other < me; other++) {
			double[][] otherEmbeddings;
			if (!useCustomTfIdf) otherEmbeddings = docProcessor.toEmbeddings(allFiles.get(other), embed, stopWords);
			else otherEmbeddings = docProcessor.toEmbeddings(allFiles.get(other), embed, stopWords, dfMapping);
			final boolean hasZeroLengthDoc = myEmbeddings.length == 0 || otherEmbeddings.length == 0;
			embedDoc = REXP.createDoubleMatrix(otherEmbeddings);
			rConn.assign("otherEmbeds", embedDoc);
			embedDoc = null;
			String myCode;
			double distance = -1.0;
			if (!hasZeroLengthDoc) {
				distance = -1.1;
				String method = "\"" + distanceFunction + "\"";
				if (myEmbeddings.length > otherEmbeddings.length) {
					myCode = "OAlign <- dtw(otherEmbeds, myEmbeds, dist.method=" + method + ", step=asymmetric, distance.only=TRUE, open.begin=TRUE, open.end=TRUE)"; 
				} else {
					myCode = "OAlign <- dtw(myEmbeds, otherEmbeds, dist.method=" + method + ", step=asymmetric, distance.only=TRUE, open.begin=TRUE, open.end=TRUE)"; 
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
