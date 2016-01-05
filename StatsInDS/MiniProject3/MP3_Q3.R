##import all the libraries
library(raster) # to get map shape file
library(ggplot2) # for plotting and miscellaneuous things
library(ggmap) # for plotting
library(plyr) # for merging datasets
library(scales) # to get nice looking legends
library(foreign)

##number of samples for single parent
n.x <- 414

##number of samples for two parent
n.y <- 501

##proportion of single parent household abuse reports
p.single.parent <- 61/414

##proportion of two parent household abuse reports
p.two.parent <- 74/501

##for 95% confidence interval
alpha <- 0.05

z.alpha <- qnorm(1-(alpha/2))

##Calc mean different
mean.diff <- p.single.parent - p.two.parent

##calculate standard error
se <- z.alpha * sqrt((p.single.parent * (1 - p.single.parent)/n.x) + (p.two.parent * (1 - p.two.parent)/n.y) )

##Calculate Confidence interval
CI <- mean.diff + c(-1,1) * se ##-0.04652425  0.04580106

## As zero is covered in the CI we can say that there is no different in the reports
