  ##import all the libraries
  library(raster) # to get map shape file
  library(ggplot2) # for plotting and miscellaneuous things
  library(ggmap) # for plotting
  library(plyr) # for merging datasets
  library(scales) # to get nice looking legends
  library(foreign)
  
  ##Function for calculating confidence interval
  conf.int<-function(pin,n,se.pin){
    pin = rbinom(n,1,pin)
    ci<- mean(pin) + c(-1, 1) * qnorm(1 - (alpha/2)) * se.pin
    return(ci)
  }
  
  ##For confidence of 95%
  alpha = 0.05
  
  ## declare n and confidence %
  nc = c(5, 10, 30, 50, 100,200,500,1000,1000)
  pc = c(0.05, 0.1, 0.25, 0.5, 0.9,0.95, 0.95,0.25,0.95)
  
  ##Loop through the pairs of n and p
  for(i in 1:length(nc)){
    n <- nc[i] ## get next n value from nc
    ##for(j in 1:length(nc)){ ##Remove comments if you want all the permutaions or else leave it commented
    j <- i ## Comment out if want all permutations
    p <- pc[j] ##get next p value from pc
    #Compute SE       
    se.p = sqrt(p*(1-p)/n)    
    
    ##Get confidence interval
    conf.int(p,n,se.p)
    
    nsim<-10000
    ci.mat<-replicate(nsim,conf.int(p,n,se.p))
    mean(p)
    ##O:/Study/CourseWork/StatsInDS/MiniProject3
    jpeg(paste('rplot_CI_',n,'_',p,'_',nsim,'.jpg',sep=""))
    ##matplot(rbind(1:100,1:100),type="l",lty=1,ci.mat[,1:100],xlab="sample",ylab="CI")
    matplot(ci.mat[,1:nsim],type="l",lty=1,rbind(1:nsim,1:nsim),ylab="sample",xlab="CI")
    abline(v=p)
    dev.off()
    calculated.p <- mean((p>=ci.mat[1,])*(p<=ci.mat[2,]))
    print(paste('For n=',n,'and p=',p,'=> Nominal P =', p, 'Calculated P =',calculated.p))
    ##}
  }