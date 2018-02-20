package br.ufpe.cin.srmqnlp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Set;

import br.cin.ufpe.nlp.util.Pair;
import br.ufpe.cin.srmqnlp.FeatureExtractorTQ.TextFeatures;
import no.uib.cipr.matrix.Matrix;

public class ExtractedTextFeaturesMatrix extends TextFilesToMatrix {

 
	private Optional<Set<Integer>> featureIndicesToUse;
	private ThreadLocal<FeatureExtractorTQ> featureExtractorLocal;
	
	public ExtractedTextFeaturesMatrix(File basePath, int vocabSize) throws IOException {
		super(basePath, vocabSize, true);
		this.featureIndicesToUse = Optional.empty();
		this.featureExtractorLocal = new ThreadLocal<FeatureExtractorTQ>() {
			protected FeatureExtractorTQ initialValue() {
				return new FeatureExtractorTQ();
			}
		};
				
				
		this.computeMatrix();
	}
	
	public ExtractedTextFeaturesMatrix(File basePath, int vocabSize, Set<Integer> featureIndicesToUse) throws IOException {
		super(basePath, vocabSize, true);
		this.featureIndicesToUse = Optional.of(featureIndicesToUse);
		this.featureExtractorLocal = new ThreadLocal<FeatureExtractorTQ>() {
			protected FeatureExtractorTQ initialValue() {
				return new FeatureExtractorTQ();
			}
		};

		this.computeMatrix();
	}
	
	@Override
	protected void processFile(File f, Matrix mat, int col) throws IOException {
		String fileString = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
		TextFeatures features = featureExtractorLocal.get().extractFeatures(fileString);
		int i = -1;
		int row = 0;
		for (Pair<String[], double[]> featureList : features.getFeatures()) {
			i++;
			if (featureIndicesToUse.isPresent() && !featureIndicesToUse.get().contains(i))
				continue;
			for (int j = 0; j < featureList.getSecond().length; j++) {
				mat.set(row, col, featureList.getSecond()[j]);
				row++;
			}
		}
	}
	public static void main(String[] args) throws IOException {
		 /**
		  * 20newsgroups-sample10 (Visual Property Features + Readability Features)
		  */ 		
		 final String basePath = args[0]; // "/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-noheaders-sample10";
		 final String outFile = args[1]; // "/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-sample10-VisualAndReadabilityFeatures-Matrix.txt";
		 final String elementListFile = args[2]; // "/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-sample10-VisualAndReadabilityFeatures-Matrix-Elements.txt";
		 
		 final int numberOfFeatures = 38;
		 ExtractedTextFeaturesMatrix mat = new ExtractedTextFeaturesMatrix(new File(basePath), numberOfFeatures);
		 mat.printOutputMatrix(outFile);
		 mat.printElementList(elementListFile);
	}
}
