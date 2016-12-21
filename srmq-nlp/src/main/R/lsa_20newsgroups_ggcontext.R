library(Matrix)
library(irlba)
ndocs <- read.table("/home/srmq/git/nlp/srmq-nlp/experiments/20newsgroups-sample10-df.txt", header=FALSE, nrows=1, colClasses=c("numeric"))
ndocs <- ndocs[1, 1]
newsgroupSampleData <- read.table("/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-sample10-Glove-GCMatrix.txt", header=FALSE, colClasses=c("integer", "integer", "numeric"), col.names=c("row", "col", "val"), skip=1)

newsSampleMatrix <- sparseMatrix(i=newsgroupSampleData$row, j=newsgroupSampleData$col, x=newsgroupSampleData$val, dims=c(300, ndocs))

rm(newsgroupSampleData)

partialSVD <- irlba(newsSampleMatrix, nv=50)
rm(newsSampleMatrix)
S <- sparseMatrix(i=c(1:50), j=c(1:50), x=partialSVD$d, dims=c(ndocs, ndocs))
W <- sparseMatrix(i=rep(1:300, times=50), j=rep(1:50,each=300), x=c(partialSVD$u), dims=c(300, ndocs))
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
writeMM(DissimsExport, file="/home/srmq/git/nlp/srmq-nlp/experiments/20newsgroups-sample10-lsa50GGContext-Dissims.mtx")
