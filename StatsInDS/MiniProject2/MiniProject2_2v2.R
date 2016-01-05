require(xlsx)
library("xlsx")

##Import dataset
# file <- system.file()
# hpi <- read.xlsx("hpi-data.xlsx", "Complete HPI Dataset")
hpi.df <- read.csv("HPI-Dataset.csv")

##plot boxplot to print
boxplot(hpi.df$HPI,hpi.df$Life,hpi.df$WellBeing,hpi.df$Footprint);
hist(hpi.df$HPI,freq=F);

##plot the scatter plot 
plot(hpi.df$HPI,hpi.df$Life);
plot(hpi.df$HPI,hpi.df$WellBeing);
plot(hpi.df$HPI,hpi.df$Footprint);

##derive correlationships between all the variables & HPI
cor(hpi.df$HPI,hpi.df$Life)
cor(hpi.df$HPI,hpi.df$WellBeing)
cor(hpi.df$HPI,hpi.df$Footprint)
