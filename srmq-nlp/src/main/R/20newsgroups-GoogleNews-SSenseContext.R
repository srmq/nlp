library(Matrix)

ndocs <- 19997
nfeat <- 300

featureData <- read.table("/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-noheaders-ssensecontext-GoogleNews-SSenseContextMatrix.txt", header=FALSE, colClasses=c("integer", "integer", "numeric"), col.names=c("row", "col", "val"), skip=1)

featureDataMatrix <- sparseMatrix(i=featureData$row, j=featureData$col, x=featureData$val, dims=c(nfeat, ndocs))

rm(featureData)

X <- t(scale(t(featureDataMatrix)))

rowSub <- apply(X, 1, function(row) all(!is.nan(row)))
X <- X[rowSub,]
nfeat <- nrow(X)

distM <- dist(t(X), diag = TRUE)

distAsMatrix <- as.matrix(distM)

distAsMatrix <- distAsMatrix/max(distAsMatrix)
DissimsExport <- as(distAsMatrix, "sparseMatrix")

writeMM(DissimsExport, file="/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-noheaders-GoogleNews-SSenseContext-Dissims.mtx")
