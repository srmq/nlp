package br.ufpe.cin.srmqnlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.carrot2.core.Document;

public class CarrotClustering {
	
	private int numberOfObjects;
	private String basePath;
	private String dissimFile;
	
	public CarrotClustering(int numberOfObjects, String basePath, String dissimFile) {
		this.numberOfObjects = numberOfObjects;
		this.basePath = basePath;
		this.dissimFile = dissimFile;
	}

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Should give 3 arguments: numberOfObjects basePath dissimFile");
			System.exit(-1);
		}
		final int nObjects = Integer.parseInt(args[0]);
		final String baseP = args[1];
		final String dissimF = args[2];
		CarrotClustering cClust = new CarrotClustering(nObjects, baseP, dissimF);

	}
	
	public void cluster() throws IOException {
		Map<Document, String> docsAndIds = new HashMap<Document, String>(numberOfObjects);
		BufferedReader bufr = null;
		try {
			bufr = new BufferedReader(new FileReader(this.dissimFile));
			for (int i = 0; i < this.numberOfObjects; i++) {
				final String line = bufr.readLine();
				//FIXME continuar
			}
		} finally {
			if (bufr != null) bufr.close();
		}
		
	}

}
