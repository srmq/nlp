#!/usr/bin/env Rscript 

# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.

# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

# Author: Sergio Queiroz (srmq at cin dot ufpe dot br)

library(optparse)
library(Matrix)
library(irlba)

option_list = list(
    make_option(c("-d", "--dfFile"), type="character", default=NULL, help="Document Frequency file name"),
    make_option(c("-w", "--wordList"), type="character", default=NULL, help="Word List file name"),
    make_option(c("-m", "--occurrMatrix"), type="character", default=NULL, help="Occurrence Matrix file name"),
    make_option(c("-a", "--withAttribute"), action="store_true", default=FALSE, help="If tokens have attribute (e.g. POSTAG) separated by an underline")
    make_option(c("-o", "--outFile"), type="character", default=NULL, help="Output file, will be written in .mtx format")
    );
    
opt_parser = OptionParser(option_list=option_list);

opt = parse_args(opt_parser);

if (is.null(opt$dfFile)) {
    print_help(opt_parser)
    stop("A document frequency file must be specified. First line in the file contains the number of documents, followed by lines with df information, one by line", call.=FALSE)
}

if (is.null(opt$wordList)) {
    print_help(opt_parser)
    stop("A word list (vocabulary) file must be specified. It should contain one word per line", call.=FALSE)
}

if (is.null(opt$occurrMatrix)) {
    print_help(opt_parser)
    stop("A Occurrence Matrix file must be specified. It should be in .mtx format, with matrix size numbe_of_words x number_of_documents", call.=FALSE)
}

if (is.null(opt$outFile)) {
    print_help(opt_parser)
    stop("An output file must be specified. It will be written in .mtx format, as a symmetric dissimilarity matrix with values normalized between 0 and 1", call.=FALSE)
}


ndocs <- read.table("/home/srmq/Documents/Research/textmining/devel/data/webkb-prepared-sample25-multilex/lemmapos-indices-extra/df.txt", header=FALSE, nrows=1, colClasses=c("numeric"))

ndocs <- ndocs[1, 1]

wordList <- readLines("/home/srmq/Documents/Research/textmining/devel/data/webkb-prepared-sample25-multilex/lemmapos-indices-extra/words.lst")

numFeatures <- length(wordList)


sampleData <- read.table("/home/srmq/Documents/Research/textmining/devel/data/webkb-prepared-sample25-multilex/lemmapos-indices-extra/webkb-prepared-sample25-MultilexLEMMAPOS-OccurMatrix.txt", header=FALSE, colClasses=c("integer", "integer", "numeric"), col.names=c("row", "col", "val"), skip=1)

sampleData$val <- log(sampleData$val)

sampleData$val <- sampleData$val + 1 

sampleMatrix <- sparseMatrix(i=sampleData$row, j=sampleData$col, x=sampleData$val, dims=c(numFeatures, ndocs))

rm(sampleData)

dfTable <- read.table("/home/srmq/Documents/Research/textmining/devel/data/webkb-prepared-sample25-multilex/lemmapos-indices-extra/df.txt", header=FALSE, skip=1, colClasses=c("numeric"))
df <- rep(0, numFeatures)
df[1:numFeatures] <- dfTable[,1]
rm(dfTable)

% aqui remover linhas referentes a stopwords

stopWords <- c("a", "about", "across", "after", "all", "almost", "also",
               "am", "among", "an", "and", "any", "are", "as", "at", "be",
               "because", "been", "but", "by", "can", "could", "did", 
               "does", "either", "else", "ever", "every", "for", "from", 
               "he", "her", "hers", "him", "his", "how", "however", "i", 
               "if", "in", "into", "is", "it", "its", "just", "least", 
               "let", "me", "most", "must", "my", "neither",
               "no", "not", "of", "off", "often", "on", "only", "or", 
               "other", "our", "rather", "she", "should", "since", "so", 
               "some", "than", "that", "the", "their", "them", "then", 
               "there", "these", "they", "this", "to", "too", "us", "was", 
               "we", "were", "what", "when", "where", "which", "while", 
               "who", "whom", "why", "will", "with", "would", "yet", 
               "you", "your", ",", ".", ";", ":", "(", ")", "[", "]", "{",
		"}", "'", "\"", "-", "?")

stopPos <- sapply(1:length(wordList), function(i) (sub(pattern="_[^_]*$", "", x=wordList[i]) %in% stopWords) | (df[i] < 3))

df <- df[!stopPos]

numFeatures <- length(df)

sampleMatrix <- sampleMatrix[!stopPos,]

df <- ifelse(df != 0, log(ndocs/df), 0)
for(i in 1:length(df)) {
    if(df[i] != 0) {
        sampleMatrix[i,] <- sampleMatrix[i,]*df[i]
    }
}

partialSVD <- irlba(sampleMatrix, nv=50)
rm(sampleMatrix)
S <- sparseMatrix(i=c(1:50), j=c(1:50), x=partialSVD$d, dims=c(ndocs, ndocs))
W <- sparseMatrix(i=rep(1:numFeatures, times=50), j=rep(1:50,each=numFeatures), x=c(partialSVD$u), dims=c(numFeatures, ndocs))
P <- sparseMatrix(i=rep(1:ndocs, times=50), j=rep(1:50,each=ndocs), x=c(partialSVD$v), dims=c(ndocs,ndocs))
rm(partialSVD)
X <- W %*% tcrossprod(S, P)
rm(P, S, W)
X <- drop0(x=X, tol=1e-15)
d<-Diagonal(x=1/sqrt(colSums(X^2)))
X <- X %*% d #normalizing all documents vector to unit size
rm(d)
Sims <- crossprod(X)
minSim <- min(Sims)
Sims <- Sims - minSim
maxSim <- max(Sims)
Sims <- Sims/maxSim
Dissims <- 1 - Sims
diag(Dissims) <- 0
DissimsExport <- as(Dissims, "sparseMatrix")
writeMM(DissimsExport, file="/home/srmq/Documents/Research/textmining/devel/data/webkb-prepared-sample25-multilex/lemmapos-indices-extra/webkb-prepared-sample25-MultilexLEMMAPOS-NoStopW-noRare3-lsa50-Dissims.mtx")
