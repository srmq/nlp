dtwWIdfEuclidianReuters <- read.csv("/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT10-DTW-CustomWIdfEuclidian-Dissim.txt", skip=2057, header=FALSE, col.names=paste0(1:2057), row.names=paste0(1:2057), colClasses="numeric")
library(Matrix)
dtwWIdfEuclidianReuters <- as.matrix(dtwWIdfEuclidianReuters)
dtwWIdfEuclidianReuters[!is.na(dtwWIdfEuclidianReuters) & dtwWIdfEuclidianReuters == -1] <- NA
minDissim <- min(dtwWIdfEuclidianReuters, na.rm=TRUE)
dtwWIdfEuclidianReuters <- ifelse(!is.na(dtwWIdfEuclidianReuters), dtwWIdfEuclidianReuters - minDissim, NA)
maxDissim <- max(dtwWIdfEuclidianReuters, na.rm=TRUE)
dtwWIdfEuclidianReuters <- ifelse(!is.na(dtwWIdfEuclidianReuters), dtwWIdfEuclidianReuters/maxDissim, NA)
dtwWIdfEuclidianReuters[row(dtwWIdfEuclidianReuters) == col(dtwWIdfEuclidianReuters)] <- 0
dtwWIdfEuclidianReuters <- ifelse(is.na(dtwWIdfEuclidianReuters), 1, dtwWIdfEuclidianReuters)
dtwWIdfEuclidianReuters <- forceSymmetric(dtwWIdfEuclidianReuters, uplo="lo")
DissimsExport <- as(dtwWIdfEuclidianReuters, "sparseMatrix")
writeMM(DissimsExport, file="/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT10-DTW-CustomWIdfEuclidian-Norm0_1.mtx")
