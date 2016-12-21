dtwReutersEuclidian <- read.csv("/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT10-DTW-Euclidian-Dissim.txt", skip=2057, header=FALSE, col.names=paste0(1:2057), row.names=paste0(1:2057), colClasses="numeric")
library(Matrix)
dtwReutersEuclidian <- as.matrix(dtwReutersEuclidian)
dtwReutersEuclidian[!is.na(dtwReutersEuclidian) & dtwReutersEuclidian == -1] <- NA
minDissim <- min(dtwReutersEuclidian, na.rm=TRUE)
dtwReutersEuclidian <- ifelse(!is.na(dtwReutersEuclidian), dtwReutersEuclidian - minDissim, NA)
maxDissim <- max(dtwReutersEuclidian, na.rm=TRUE)
dtwReutersEuclidian <- ifelse(!is.na(dtwReutersEuclidian), dtwReutersEuclidian/maxDissim, NA)
dtwReutersEuclidian[row(dtwReutersEuclidian) == col(dtwReutersEuclidian)] <- 0
dtwReutersEuclidian <- ifelse(is.na(dtwReutersEuclidian), 1, dtwReutersEuclidian)
dtwReutersEuclidian <- forceSymmetric(dtwReutersEuclidian, uplo="lo")
DissimsExport <- as(dtwReutersEuclidian, "sparseMatrix")
writeMM(DissimsExport, file="/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT10-DTW-SimpleEuclidian-Dissims-Norm0_1.mtx")
