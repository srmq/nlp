package br.ufpe.cin.srmqnlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.carrot2.clustering.kmeans.BisectingKMeansClusteringAlgorithm;
import org.carrot2.core.Cluster;
import org.carrot2.core.Controller;
import org.carrot2.core.ControllerFactory;
import org.carrot2.core.Document;
import org.carrot2.core.ProcessingResult;
import org.carrot2.core.attribute.CommonAttributesDescriptor;

public class CarrotClustering {
	
	private int numberOfObjects;
	private String basePath;
	private String dissimFile;
	
	public CarrotClustering(int numberOfObjects, String basePath, String dissimFile) {
		this.numberOfObjects = numberOfObjects;
		this.basePath = basePath;
		this.dissimFile = dissimFile;
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.err.println("Should give 3 arguments: numberOfObjects basePath dissimFile");
			System.exit(-1);
		}
		final int nObjects = Integer.parseInt(args[0]);
		final String baseP = args[1];
		final String dissimF = args[2];
		CarrotClustering cClust = new CarrotClustering(nObjects, baseP, dissimF);
		cClust.cluster();

	}
	
	public void cluster() throws IOException {
		Map<Document, String> docsAndIds = new HashMap<Document, String>(numberOfObjects);
		BufferedReader bufr = null;
		try {
			bufr = new BufferedReader(new FileReader(this.dissimFile));
			for (int i = 0; i < this.numberOfObjects; i++) {
				final String line = bufr.readLine();
				StringTokenizer sttok = new StringTokenizer(line, "\"");
				sttok.nextToken();
				String docId = sttok.nextToken();
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
			final Controller controller = ControllerFactory.createSimple();
			final Map<String, Object> attributes = new HashMap<String, Object>();
			final Set<Document> docsToCluster = docsAndIds.keySet();
			System.out.println("INFO: We have " + docsToCluster.size() + " documents to cluster");
			attributes.put(CommonAttributesDescriptor.Keys.DOCUMENTS, new ArrayList<Document>(docsToCluster));
			attributes.put("BisectingKMeansClusteringAlgorithm.clusterCount", 20);
			ProcessingResult result = controller.process(attributes, BisectingKMeansClusteringAlgorithm.class);
			List<Cluster> clusters = result.getClusters();
			//CarrotConsoleFormatter.displayClusters(clusters);
			int docCount = 0;
			for (int i = 0; i < clusters.size(); i++) {
				final Cluster clusterI = clusters.get(i);
				if (clusterI.getSubclusters() != null && clusterI.getSubclusters().size() > 0) {
					System.out.println("INFO: Cluster " + i + " has subclusters");
				}
				System.out.println("Cluster " + i + ":");
				System.out.println(clusterI.getLabel());
				List<Document> docs = clusterI.getAllDocuments();
				docCount += docs.size();
				for (Document document : docs) {
					String docId = docsAndIds.get(document);
					System.out.println(docId);
				}
				System.out.println("");
			}
			System.out.println("Total document count in clusters: " + docCount);
			
		} finally {
			if (bufr != null) bufr.close();
		}
		
	}

}
