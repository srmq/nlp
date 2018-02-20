library(Matrix)

ndocs <- 2009
nfeat <- 300

newsgroupSampleStyleData <- read.table("/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-sample10-Glove-SSenseContextMatrix.txt", header=FALSE, colClasses=c("integer", "integer", "numeric"), col.names=c("row", "col", "val"), skip=1)

newsSampleMatrix <- sparseMatrix(i=newsgroupSampleStyleData$row, j=newsgroupSampleStyleData$col, x=newsgroupSampleStyleData$val, dims=c(nfeat, ndocs))

rm(newsgroupSampleStyleData)

X <- t(scale(t(newsSampleMatrix)))

rowSub <- apply(X, 1, function(row) all(!is.nan(row)))
X <- X[rowSub,]
nfeat <- nrow(X)

distM <- dist(t(X), diag = TRUE)

distAsMatrix <- as.matrix(distM)

distAsMatrix <- distAsMatrix/max(distAsMatrix)
DissimsExport <- as(distAsMatrix, "sparseMatrix")

writeMM(DissimsExport, file="/home/srmq/Documents/Research/textmining/devel/data/20_newsgroups-sample10-SSenseContext-Dissims.mtx")
