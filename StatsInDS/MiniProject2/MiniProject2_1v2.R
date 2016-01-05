##import all the libraries
library(raster) # to get map shape file
library(ggplot2) # for plotting and miscellaneuous things
library(ggmap) # for plotting
library(plyr) # for merging datasets
library(scales) # to get nice looking legends
library(maps)
library(foreign)
library(maps)

##get state map data
usa.df <- map_data("state")

# look at the data structure
str(usa.df)

# Notes: 
# 1. usa.df is already a dataframe, so there is no need to 'fortify' it.
# 2. Let's rename 'region' as 'state' and make it a factor variable (more on this in class later)
colnames(usa.df) [5] <- "state"
usa.df$state <- as.factor(usa.df$state)

# look at the data structure again
str(usa.df)

# look at the levels of the state variable
levels(usa.df$state)

##import the dataset 
usa.dat <- read.dta("usstatesWTID.dta")

##look at imported data
str(usa.dat)

##Change "State" -> "state" 
colnames(usa.dat)[3] <- "state"

##Conver state names to lower case
usa.dat$state <- tolower(usa.dat$state)

##Factor state column
usa.dat$state <- as.factor(usa.dat$state)

##check for the levels
levels(usa.dat$state)

##take a look at state column
str(usa.dat$state)

## check range of state column
range(usa.dat$state)

# Merge the shape data with the population data by state name
usa.df.joined <- join(usa.df, usa.dat, by = "state", type = "inner")

##check the joined data
str(usa.df.joined)

str(usa.df.joined$state[1])

usa.df.joined.2012 <- usa.df.joined[usa.df.joined$Year=="2012",]

usa.df.joined.1999 <- usa.df.joined[usa.df.joined$Year=="1999",]

##Rename top1 column name to differentiate 2012 and 1999
colnames(usa.df.joined.2012)[11] <- "top1_adj_2012"
colnames(usa.df.joined.1999)[11] <- "top1_adj_1999"

## join by state, lat, long, group and order to take different  of top1_adj_2012 - top1_adj_1999
usa.df.joined.20121999 <- join(usa.df.joined.2012, usa.df.joined.1999, by = c("state","lat","long","group","order"), type = "inner")
usa.df.joined.20121999$top1_diff_2012_1999 <- usa.df.joined.2012$top1_adj_2012 - usa.df.joined.1999$top1_adj_1999

##get state abbreviations
state.name.tolower <- tolower(state.name)
usa.df.joined.2012$stateAbbr <- state.abb[match(usa.df.joined.2012$state, state.name.tolower)] #get state abbreviations
usa.df.joined.1999$stateAbbr <- state.abb[match(usa.df.joined.1999$state, state.name.tolower)] #get state abbreviations
usa.df.joined.20121999$stateAbbr <- state.abb[match(usa.df.joined.20121999$state, state.name.tolower)] #get state abbreviations

##Find the mean of all lat and longs by state to print the state abbreviations
meanLat <- aggregate(usa.df$lat, by=list(state=usa.df$state), FUN=)
meanLong <- aggregate(usa.df$long, by=list(state=usa.df$state), FUN=mean)

##change then name of x column i.e. aggregeated column to lat and long respectively
colnames(meanLat)[2] <- "lat"
colnames(meanLong)[2] <- "long"

##join the aggregated lat and long DFs 
meanLatLong2 <- join(meanLat, meanLong, by="state", type="inner")
meanLatLong <- aggregate(cbind(long, lat) ~ state, data=usa.df,FUN=function(x)mean(range(x)))
meanLatLong$stateAbbr <- state.abb[match(meanLatLong$state, state.name.tolower)] #get state abbreviations

##Print/plot map for year 2012
p <- ggplot() +
  # with borders (slower)
  geom_polygon(data = usa.df.joined.2012, aes(x = long, y = lat, group = group, fill = top1_adj_2012), 
               color = "black", size = 0.15) +
  # without borders(faster)
  # geom_polygon(data = usa.df.joined, aes(x = long, y = lat, group = group, fill = sexratio), 
  #		color = "black", size = 0.25) +	
  geom_text(data=meanLatLong, aes(x = long, y = lat,label=stateAbbr),
            color = "blue", size=3)+
  scale_fill_distiller(palette = "Reds", trans = "reverse") +
  theme_nothing(legend = TRUE) +
  labs(title = "State level income share of top 1% in year 2012", fill = "")	

ggsave(p, file = "top1_2012.pdf")#Save map

## Print/Plot map for year 1999
p <- ggplot() +
  # with borders (slower)
  geom_polygon(data = usa.df.joined.1999, aes(x = long, y = lat, group = group, fill = top1_adj_1999), 
               color = "black", size = 0.15) +
  geom_text(data=meanLatLong, aes(x = long, y = lat,label=stateAbbr),
            color = "blue", size=3)+
  scale_fill_distiller(palette = "Reds", trans = "reverse") +
  theme_nothing(legend = TRUE) +
  labs(title = "State level income share of top 1% in year 1999", fill = "")	

ggsave(p, file = "top1_1999.pdf")#Save map

##Print/plot map for difference of 2012 - 1999
p <- ggplot() +
  # with borders (slower)
  geom_polygon(data = usa.df.joined.20121999, aes(x = long, y = lat, group = group, fill = top1_diff_2012_1999), 
               color = "black", size = 0.15) +
  geom_text(data=meanLatLong, aes(x = long, y = lat,label=stateAbbr),
            color = "blue", size=3)+
  scale_fill_distiller(palette = "Reds", trans = "reverse") +
  theme_nothing(legend = TRUE) +
  labs(title = "Difference in State level income share of top 1% from 1999 to 2012", fill = "")	

ggsave(p, file = "top1_2012-1999.pdf") #Save map


