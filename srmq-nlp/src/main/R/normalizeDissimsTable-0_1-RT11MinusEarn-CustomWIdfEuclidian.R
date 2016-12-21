dtwCustomWIdfEuclidianReuters <- read.csv("/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT11MinusEarn-DTW-CustomWIdfEuclidian-Dissim.txt", skip=1034, header=FALSE, col.names=paste0(1:1034), row.names=paste0(1:1034), colClasses="numeric")
library(Matrix)
dtwCustomWIdfEuclidianReuters <- as.matrix(dtwCustomWIdfEuclidianReuters)
dtwCustomWIdfEuclidianReuters[!is.na(dtwCustomWIdfEuclidianReuters) & dtwCustomWIdfEuclidianReuters == -1] <- NA
minDissim <- min(dtwCustomWIdfEuclidianReuters, na.rm=TRUE)
dtwCustomWIdfEuclidianReuters <- ifelse(!is.na(dtwCustomWIdfEuclidianReuters), dtwCustomWIdfEuclidianReuters - minDissim, NA)
maxDissim <- max(dtwCustomWIdfEuclidianReuters, na.rm=TRUE)
dtwCustomWIdfEuclidianReuters <- ifelse(!is.na(dtwCustomWIdfEuclidianReuters), dtwCustomWIdfEuclidianReuters/maxDissim, NA)
dtwCustomWIdfEuclidianReuters[row(dtwCustomWIdfEuclidianReuters) == col(dtwCustomWIdfEuclidianReuters)] <- 0
dtwCustomWIdfEuclidianReuters <- ifelse(is.na(dtwCustomWIdfEuclidianReuters), 1, dtwCustomWIdfEuclidianReuters)
dtwCustomWIdfEuclidianReuters <- forceSymmetric(dtwCustomWIdfEuclidianReuters, uplo="lo")
DissimsExport <- as(dtwCustomWIdfEuclidianReuters, "sparseMatrix")
writeMM(DissimsExport, file="/home/srmq/git/nlp/srmq-nlp/experiments/dtw201609/reuters21578-RT11MinusEarn-DTW-CustomWIdfEuclidian-Dissims-Norm0_1.mtx")
