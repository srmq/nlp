dtwEuclidianReuters <- read.csv("/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT10-DTW-Euclidian-Dissim.txt", skip=2057, header=FALSE, col.names=paste0(1:2057), row.names=paste0(1:2057), colClasses="numeric")
library(Matrix)
dtwEuclidianReuters <- as.matrix(dtwEuclidianReuters)
dtwEuclidianReuters[!is.na(dtwEuclidianReuters) & dtwEuclidianReuters == -1] <- NA
minDissim <- min(dtwEuclidianReuters, na.rm=TRUE)
dtwEuclidianReuters <- ifelse(!is.na(dtwEuclidianReuters), dtwEuclidianReuters - minDissim, NA)
maxDissim <- max(dtwEuclidianReuters, na.rm=TRUE)
dtwEuclidianReuters <- ifelse(!is.na(dtwEuclidianReuters), dtwEuclidianReuters/maxDissim, NA)
dtwEuclidianReuters[row(dtwEuclidianReuters) == col(dtwEuclidianReuters)] <- 0
dtwEuclidianReuters <- ifelse(is.na(dtwEuclidianReuters), 1, dtwEuclidianReuters)
dtwEuclidianReuters <- forceSymmetric(dtwEuclidianReuters, uplo="lo")
DissimsExport <- as(dtwEuclidianReuters, "sparseMatrix")
writeMM(DissimsExport, file="/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT10-DTW-Euclidian-Dissims-Norm0_1.mtx")
