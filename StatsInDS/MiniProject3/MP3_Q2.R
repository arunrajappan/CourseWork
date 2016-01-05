##import all the libraries
library(raster) # to get map shape file
library(ggplot2) # for plotting and miscellaneuous things
library(ggmap) # for plotting
library(plyr) # for merging datasets
library(scales) # to get nice looking legends
library(foreign)

child_cereal <- c(40.3, 55, 45.7, 43.3, 50.3, 45.9, 53.5, 43, 44.2, 44, 47.4, 44, 33.6, 55.1,
                  48.8, 50.4, 37.8, 60.3, 46.5)
adult_cereal <- c(20, 30.2, 2.2, 7.5, 4.4, 22.2, 16.6, 14.5, 21.4, 3.3, 6.6, 7.8, 10.6, 16.2,
                  14.5, 4.1, 15.8, 4.1, 2.4, 3.5, 8.5, 10, 1, 4.4, 1.3, 8.1, 4.7, 18.4)

##Test Normal Distribution of Child Cereal samples
qqnorm(child_cereal, xlab = 'Theoretical Quantiles' ,ylab = 'Chile Cereal Samples')

##Check normal distribution of Adult Cereal samples
qqnorm(adult_cereal, xlab = 'Theoretical Quantiles' ,ylab = 'Adult Cereal Samples')

## Test Normal distribution of both Child and adult samples
qqplot(child_cereal, adult_cereal, plot.it = TRUE, xlab = deparse(substitute(child_cereal)), ylab = deparse(substitute(adult_cereal)))

##child cereal variance
var_child_cereal <- var(child_cereal)

##Adult cereal variance
var_adult_cereal <- var(adult_cereal)

## F-test to test if the variances of these 2 ditribution are equal
var.test(child_cereal, adult_cereal) ##Here P-Value = 0.4566 for 95% confidence interval so variance can be assumed equal

t.test(child_cereal, adult_cereal, paired = TRUE, conf.level = 0.95)

m1 <- mean(child_cereal) ##Mean of child cereal samples
m2 <- mean(adult_cereal) ##Mean of adult cereal samples

n1 <- length(child_cereal) ## Number of Child Cereal Samples
n2 <- length(adult_cereal) ## Number of adult Cereal Samples

##Calculate Pooled Sample variance
pooled_var <- ((n2-1)*var(child_cereal) + (n1-1)*var(adult_cereal))/(n1+n2-2)

##Pooled Standard Deviation
pooled_sd <- sqrt(pooled_var)

##Degrees of freedom
dfV <- n1 + n2 -2

##T-Distribution 
tDist <- qt(1- 0.025, df = dfV)

mean_diff <- m1-m2

##Standard Error
se <- tDist * pooled_sd * sqrt(1/n1 + 1/n2)

##Confidence interval lower bound
CI_Lower <- mean_diff - se ##32.49797

##Confidence interval upper bound
CI_Upper <- mean_diff + se ##40.78436

##Confidence Interval (just for fun)
CI <- mean_diff + c(-1.0,1.0) * se ## 32.49797 40.78436
## From the above CI we can say that mean sugar level in child cereals is a lot more then the adult cereal