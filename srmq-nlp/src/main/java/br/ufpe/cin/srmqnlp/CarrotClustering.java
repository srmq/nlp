package br.ufpe.cin.srmqnlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.carrot2.clustering.kmeans.BisectingKMeansClusteringAlgorithm;
import org.carrot2.clustering.kmeans.BisectingKMeansClusteringAlgorithmDescriptor;
import org.carrot2.core.Cluster;
import org.carrot2.core.Controller;
import org.carrot2.core.ControllerFactory;
import org.carrot2.core.Document;
import org.carrot2.core.ProcessingResult;
import org.carrot2.core.attribute.CommonAttributesDescriptor;

import br.cin.ufpe.nlp.util.Pair;
import wrdca.util.ConfusionMatrix;

public class CarrotClustering {
	
	private int n;
	private String basePath;
	private String configFile;
	private int numIteracoes;
	private int k;
	private int numPrioriClusters;
	private List<File> inputFiles;
	private File outputFile;
	private int numInicializacao;

	
	public CarrotClustering(String basePath, String configFile) throws IOException {
		this.basePath = basePath;
		this.configFile = configFile;
		readConfigFile(this.configFile);
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Should give 2 arguments: basePath configFile");
			System.exit(-1);
		}
		final String baseP = args[0];
		final String configF = args[1];
		CarrotClustering cClust = new CarrotClustering(baseP, configF);
		cClust.cluster();
		System.exit(0);

	}
	
	public void cluster() throws IOException {
		Map<Document, String> docsAndIds = new HashMap<Document, String>(n);
		BufferedReader bufr = null;
		PrintStream outStream = new PrintStream(this.outputFile, "UTF-8");;
		Map<String, Pair<Integer, String>> idDocument2IndexDocumentAndIdClass = new HashMap<String, Pair<Integer, String>>(n);
		try {
			bufr = new BufferedReader(new FileReader(this.inputFiles.get(0)));
			for (int i = 0; i < this.n; i++) {
				final String line = bufr.readLine();
				StringTokenizer sttok = new StringTokenizer(line, "\"");
				String idClass = sttok.nextToken();
				idClass = idClass.substring(0, idClass.lastIndexOf(','));
				String docId = sttok.nextToken();
				idDocument2IndexDocumentAndIdClass.put(docId, new Pair<Integer, String>(i, idClass));
				File docFile = new File(this.basePath + File.separator + docId);
				StringBuffer stb = new StringBuffer((int)docFile.length());
				BufferedReader bufrFile = new BufferedReader(new FileReader(docFile));
				String lineFile;
				while ((lineFile = bufrFile.readLine()) != null) {
					stb.append(lineFile);
					stb.append(' ');
				}
				bufrFile.close();
				Document carrotDocument = new Document(stb.toString());
				docsAndIds.put(carrotDocument,	docId);
			}
			long timeInMilis = System.currentTimeMillis();
			final Set<Document> docsToCluster = docsAndIds.keySet();
			outStream.println("INFO: We have " + docsToCluster.size() + " documents to cluster");
			List<Cluster> bestClusters = null;
			double bestScore = -1;
			
			for (int init = 0; init < numInicializacao; init++) {
				final Controller controller = ControllerFactory.createSimple();
				final Map<String, Object> attributes = new HashMap<String, Object>();
				attributes.put(CommonAttributesDescriptor.Keys.DOCUMENTS, new ArrayList<Document>(docsToCluster));
				attributes.put(BisectingKMeansClusteringAlgorithmDescriptor.Keys.CLUSTER_COUNT, k);
				attributes.put(BisectingKMeansClusteringAlgorithmDescriptor.Keys.USE_INITIAL_RANDOM_ASSIGNMENT, true);
				if (this.numIteracoes > 0)
					attributes.put(BisectingKMeansClusteringAlgorithmDescriptor.Keys.MAX_ITERATIONS, this.numIteracoes);
				ProcessingResult result = controller.process(attributes, BisectingKMeansClusteringAlgorithm.class);
				List<Cluster> clusters = result.getClusters();
				double myScore;
				if ((myScore = totalSimilarity(clusters)) > bestScore) {
					bestClusters = clusters;
					bestScore = myScore;
				}
				outStream.println("INFO: Run number " + init + ", score: " + myScore);
			}
			timeInMilis = System.currentTimeMillis() - timeInMilis;
			//CarrotConsoleFormatter.displayClusters(clusters);
			int docCount = 0;
			ConfusionMatrix confusionMatrix = new ConfusionMatrix(k, numPrioriClusters);
			for (int i = 0; i < bestClusters.size(); i++) {
				final Cluster clusterI = bestClusters.get(i);
				if (clusterI.getSubclusters() != null && clusterI.getSubclusters().size() > 0) {
					outStream.println("INFO: Cluster " + i + " has subclusters");
				}
				outStream.println("Cluster " + i + ":");
				outStream.println(clusterI.getLabel());
				List<Document> docs = clusterI.getAllDocuments();
				docCount += docs.size();
				for (Document document : docs) {
					final String docId = docsAndIds.get(document);
					final Pair<Integer, String> myDocumentIdAndClassId = idDocument2IndexDocumentAndIdClass.get(docId);
					confusionMatrix.putObject(myDocumentIdAndClassId.getFirst(), i, Integer.parseInt(myDocumentIdAndClassId.getSecond()));
					outStream.println(docId);
				}
				outStream.println("");
			}
			outStream.println("Total document count in clusters: " + docCount);
			outStream.println("Best score was: " + bestScore);
			outStream.println("------CONFUSION MATRIX-------");
			confusionMatrix.printMatrix(outStream);
			outStream.println("-----------------------------");
			outStream.println(">>>>>>>>>>>> The F-Measure is: "+ confusionMatrix.fMeasureGlobal());
			outStream.println(">>>>>>>>>>>> The CR-Index  is: "+ confusionMatrix.CRIndex());
			outStream.println(">>>>>>>>>>>> OERC Index    is: " + confusionMatrix.OERCIndex());
			outStream.println(">>>>>>>>>>>> NMI  Index    is: " + confusionMatrix.nMIIndex());;
			outStream.println("Total time in seconds: " + timeInMilis/1000.0);
			outStream.flush();
			outStream.close();
			System.exit(0);

			
		} finally {
			if (bufr != null) bufr.close();
		}
		
	}
	
	private double totalSimilarity(List<Cluster> clusters) {
		double sum = 0.0;
		for (Cluster cluster : clusters) {
			sum += cluster.getScore();
		}
		return sum;
	}

	private void readConfigFile(String file)
			throws FileNotFoundException, IOException {
		File configFile = new File(file);
		BufferedReader bufw = new BufferedReader(new FileReader(configFile));
		String line;
		while ((line = bufw.readLine()) != null) {
			if (line.contains("(numCluster)")) {
				k = Integer.parseInt(bufw.readLine());
			} else if (line.contains("(numInicializacao)")) {
				numInicializacao = Integer.parseInt(bufw.readLine());
			} else if (line.contains("(numIteracoes)")) {
				numIteracoes = Integer.parseInt(bufw.readLine());
			} else if (line.contains("(input)")) {
				inputFiles = new LinkedList<File>();
				while ((line = bufw.readLine()).length() > 0) {
					inputFiles.add(new File(line));
				}
			} else if (line.contains("(output)")) {
				outputFile = new File(bufw.readLine());
			} else if (line.contains("(numIndividuos)")) {
				n = Integer.parseInt(bufw.readLine());
			} else if (line.contains("(numPrioriClusters)")) {
				numPrioriClusters = Integer.parseInt(bufw.readLine());
			}
		}
		
		bufw.close();
	}
	

}
