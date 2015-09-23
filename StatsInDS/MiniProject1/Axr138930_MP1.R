######################################################################################################
# Mini Project - 1
# Name - Arunkumar Rajappan
# Description : Conduuct a Monte Carlo simulation to study using R to obtain answer for 
#     exercise 4.6 - A program is divided into 3 blocks that are being compiled on 3 parallel computers. 
#     Each block takes an Exponential amount of time, 5 minutes on the average, independently of
#     other blocks. The program is completed when all the blocks are compiled. Compute the
#     expected time it takes the program to be compiled.
######################################################################################################

N <- c(1000,10000,100000) #Number of executions/Samples
lambda <- 1/5 #rate of the exponential function

for(n in N){
  x <- c() #Final X value
  ##Generate N Samples
  for(i in 1:n){
    # Xi <- rexp(3,rate = lambda) #Taking all the 3 observations
    X1 <- rexp(1,rate = lambda) #Generte random observation-1
    X2 <- rexp(1,rate = lambda) #Generte random observation-2
    X3 <- rexp(1,rate = lambda) #Generte random observation-3
    
    # print(paste(Xi,max(Xi)))
    # print(paste(X1,X2,X3))
    
    # x <- c(x, max(Xi)) #Get Max observation and append to 
    x <- c(x, max(X1,X2,X3)) #Get Max observation and append to 
    
  }
  
  # print(x)
  #Plot histogram of all draws
  res <- hist(x,freq=FALSE) #Freq is false to plot densities
  # print(str(trellis.panelArgs(res, 2)))
  
  curve ( 0.6 * (1-exp(-0.2*x)^2) * exp(-0.2*x),0,res$breaks[length(res$breaks)],add=TRUE) #superimpose PDF on histogram 
  Ex <- mean(x) #Get mean/expectation of the draws
  print(paste('E(x) = ', Ex))
  
  # Sys.sleep(5) #Pause the program for 5secs
}

## End Project