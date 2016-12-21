package br.ufpe.cin.srmqnlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import no.uib.cipr.matrix.Matrix;

public class TextOccurencesMatrix extends TextFilesToMatrix {
	
	public TextOccurencesMatrix(File basePath, int vocabSize) throws IOException {
		super(basePath, vocabSize);
	}

	@Override
	protected void processFile(File f, Matrix mat, int col) throws IOException {
		BufferedReader bufR = new BufferedReader(new FileReader(f));
		String line;
		while ((line = bufR.readLine()) != null) {
			int index = Integer.parseInt(line.trim());
			index--;
			mat.add(index, col, 1.0);
		}
		bufR.close();
	}

	public static void main(String[] args) throws IOException{
		/**
		 * 20 Newsgroups
		 * final String basePath = "/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-noheaders-indices-sample10";
		 * final String outFile = "/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-noheaders-sample10-occurMatrix.txt";
		 * final String elementListFile = "/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-noheaders-sample10-matrixElements.txt";
		 */
		
		/**
		 * Reuters RT10
		 * final String basePath = "/home/srmq/Documents/Research/textmining/devel/data/reuters21578/ModApteTestWithBodySingleTopic/sennaIndices/top10Categories";
		 * final String outFile = "/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT10-LSA/reuters21578-RT10-OccurMatrix.txt";
		 * final String elementListFile = "/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT10-LSA/reuters21578-RT10-OccurMatrix-Elements.txt";
		 */

		final String basePath = "/home/srmq/Documents/Research/textmining/devel/data/reuters21578/ModApteTestWithBodySingleTopic/sennaIndices/top11MinusEarn";
		final String outFile = "/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT11MinusEarn-LSA/reuters21578-RT11MinusEarn-OccurMatrix.txt";
		final String elementListFile = "/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT11MinusEarn-LSA/reuters21578-RT11MinusEarn-OccurMatrix-Elements.txt";
		int vocabSize;
		{
			final String vocab = "/home/srmq/devel/senna/hash/words.lst";
			BufferedReader bufR = new BufferedReader(new FileReader(vocab));
			vocabSize = 0;
			while(bufR.readLine() != null) vocabSize++;
			bufR.close();
		}
		System.out.println("Vocabulary size is " + vocabSize);
		TextOccurencesMatrix textMatrix = new TextOccurencesMatrix(new File(basePath), vocabSize);
		textMatrix.printOutputMatrix(outFile);
		textMatrix.printElementList(elementListFile);
		/*
		 * Exemplo lendo no R como data.frame: 
		 * 
		 * library(Matrix)
		 * library(irlba)
		 * newsgroupSampleData <- read.table("/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-noheaders-sample10-occurMatrix.txt", header=FALSE, colClasses=c("integer", "integer", "numeric"), col.names=c("row", "col", "val"), skip=1)
		 * newsgroupSampleData$val <- log(newsgroupSampleData$val)
		 * newsgroupSampleData$val <- newsgroupSampleData$val + 1 //FIXME multiplicar por ln(n/(df_xi))
		 * newsSampleMatrix <- sparseMatrix(i=newsgroupSampleData$row, j=newsgroupSampleData$col, x=newsgroupSampleData$val, dims=c(130000, 2009))
		 * rm(newsgroupSampleData)

		 * ndocs <- read.table("/home/srmq/git/nlp/srmq-nlp/experiments/20newsgroups-sample10-df.txt", header=FALSE, nrows=1, colClasses=c("numeric"))
		 * ndocs <- ndocs[1, 1]
		 * dfTable <- read.table("/home/srmq/git/nlp/srmq-nlp/experiments/20newsgroups-sample10-df.txt", header=FALSE, skip=1, colClasses=c("numeric"))
		 * df <- rep(0, 130000)
		 * df[1:129998] <- dfTable[,1]
		 * rm(dfTable)
		 * df <- ifelse(df != 0, log(ndocs/df), 0)
		 * df <- as(df, "sparseVector")
 
		 * 
		 * partialSVD <- irlba(newsSampleMatrix, nv=50)
		 * rm(newsSampleMatrix)
		 * S <- sparseMatrix(i=c(1:50), j=c(1:50), x=partialSVD$d, dims=c(2009, 2009))
		 * W <- sparseMatrix(i=rep(1:130000, times=50), j=rep(1:50,each=130000), x=c(partialSVD$u), dims=c(130000, 2009))
		 * P <- sparseMatrix(i=rep(1:2009, times=50), j=rep(1:50,each=2009), x=c(partialSVD$v), dims=c(2009,2009))
		 * rm(partialSVD)
		 * X <- W %*% tcrossprod(S, P)
		 * rm(P, S, W)
		 * X <- drop0(x=X, tol=1e-15)
		 * d<-Diagonal(x=1/sqrt(colSums(X^2)))
		 * X <- X %*% d #normalizing all documents vector to unit size
		 * rm(d)
		 * Sims <- crossprod(X)
		 */
	}

}
