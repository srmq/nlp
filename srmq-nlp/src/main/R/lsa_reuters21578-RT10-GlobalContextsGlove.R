library(Matrix)
library(irlba)
ndocs <- read.table("/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT10-df.txt", header=FALSE, nrows=1, colClasses=c("numeric"))
ndocs <- ndocs[1, 1]
reutersRT10GloveGlobalContextData <- read.table("/home/srmq/Documents/Research/textmining/devel/data/reuters21578/ModApteTestWithBodySingleTopic/globalContextsGlove/reuters21578-RT10-Glove-GCMatrix.txt", header=FALSE, colClasses=c("integer", "integer", "numeric"), col.names=c("row", "col", "val"), skip=1)

reutersSampleMatrix <- sparseMatrix(i=reutersRT10GloveGlobalContextData$row, j=reutersRT10GloveGlobalContextData$col, x=reutersRT10GloveGlobalContextData$val, dims=c(300, ndocs))

rm(reutersRT10GloveGlobalContextData)

partialSVD <- irlba(reutersSampleMatrix, nv=50)
rm(reutersSampleMatrix)
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
writeMM(DissimsExport, file="/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT10-LSA-GloveGlobalContext/reuters21578-RT10-lsa50-GloveGlobalContext-Dissims.mtx")
