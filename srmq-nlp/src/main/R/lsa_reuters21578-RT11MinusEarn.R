library(Matrix)
library(irlba)
ndocs <- read.table("/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT11MinusEarn-df.txt", header=FALSE, nrows=1, colClasses=c("numeric"))
ndocs <- ndocs[1, 1]
reutersRT11MinusEarnData <- read.table("/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT11MinusEarn-LSA/reuters21578-RT11MinusEarn-OccurMatrix.txt", header=FALSE, colClasses=c("integer", "integer", "numeric"), col.names=c("row", "col", "val"), skip=1)

reutersRT11MinusEarnData$val <- log(reutersRT11MinusEarnData$val)

reutersRT11MinusEarnData$val <- reutersRT11MinusEarnData$val + 1
 
reutersSampleMatrix <- sparseMatrix(i=reutersRT11MinusEarnData$row, j=reutersRT11MinusEarnData$col, x=reutersRT11MinusEarnData$val, dims=c(130000, ndocs))

rm(reutersRT11MinusEarnData)

dfTable <- read.table("/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT11MinusEarn-df.txt", header=FALSE, skip=1, colClasses=c("numeric"))
df <- rep(0, 130000)
df[1:129941] <- dfTable[,1]
rm(dfTable)
df <- ifelse(df != 0, log(ndocs/df), 0)
for(i in 1:length(df)) {
    if(df[i] != 0) {
        reutersSampleMatrix[i,] <- reutersSampleMatrix[i,]*df[i]
    }
}

partialSVD <- irlba(reutersSampleMatrix, nv=50)
rm(reutersSampleMatrix)
S <- sparseMatrix(i=c(1:50), j=c(1:50), x=partialSVD$d, dims=c(ndocs, ndocs))
W <- sparseMatrix(i=rep(1:130000, times=50), j=rep(1:50,each=130000), x=c(partialSVD$u), dims=c(130000, ndocs))
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
writeMM(DissimsExport, file="/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT11MinusEarn-LSA/reuters21578-RT11MinusEarn-lsa50-Dissims.mtx")
