library(Matrix)
library(irlba)

ndocs <- read.table("/home/srmq/Documents/Research/textmining/devel/data/reuters21578/ModApteTestWithBodySingleTopic/multilex-tokenization-indices/lemmas-extra/df.txt", header=FALSE, nrows=1, colClasses=c("numeric"))

ndocs <- ndocs[1, 1]

wordList <- readLines("/home/srmq/Documents/Research/textmining/devel/data/reuters21578/ModApteTestWithBodySingleTopic/multilex-tokenization-indices/lemmas-extra/words.lst")

numFeatures <- length(wordList)


newsgroupSampleData <- read.table("/home/srmq/Documents/Research/textmining/devel/data/reuters21578/ModApteTestWithBodySingleTopic/multilex-tokenization-indices/lemmas-extra/reuters21578-MultilexLEMMAS-OccurMatrix.txt", header=FALSE, colClasses=c("integer", "integer", "numeric"), col.names=c("row", "col", "val"), skip=1)

newsgroupSampleData$val <- log(newsgroupSampleData$val)

newsgroupSampleData$val <- newsgroupSampleData$val + 1 

newsSampleMatrix <- sparseMatrix(i=newsgroupSampleData$row, j=newsgroupSampleData$col, x=newsgroupSampleData$val, dims=c(numFeatures, ndocs))

rm(newsgroupSampleData)

dfTable <- read.table("/home/srmq/Documents/Research/textmining/devel/data/reuters21578/ModApteTestWithBodySingleTopic/multilex-tokenization-indices/lemmas-extra/df.txt", header=FALSE, skip=1, colClasses=c("numeric"))
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

stopPos <- sapply(1:length(wordList), function(i) (wordList[i] %in% stopWords) | (df[i] < 3))

df <- df[!stopPos]

numFeatures <- length(df)

newsSampleMatrix <- newsSampleMatrix[!stopPos,]

df <- ifelse(df != 0, log(ndocs/df), 0)
for(i in 1:length(df)) {
    if(df[i] != 0) {
        newsSampleMatrix[i,] <- newsSampleMatrix[i,]*df[i]
    }
}

partialSVD <- irlba(newsSampleMatrix, nv=50)
rm(newsSampleMatrix)
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
writeMM(DissimsExport, file="/home/srmq/Documents/Research/textmining/devel/data/reuters21578/ModApteTestWithBodySingleTopic/multilex-tokenization-indices/lemmas-extra/reuters21578-RT10-MultilexLEMMAS-NoStopW-noRare3-lsa50-Dissims.mtx")
