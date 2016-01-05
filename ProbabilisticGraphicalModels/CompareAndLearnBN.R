library(bnlearn)
library(Rgraphviz)
library(microbenchmark)

args <- commandArgs(trailingOnly = TRUE)
sink(file="E:/ProjectPGM/MyCode/log_insurance2.rlog",append=FALSE)
sink()
##rgs <- array(c("asia"),c(1,1))
print("Command args are == ")
print(args)
debugFlag <- TRUE

# vDatasets <- list()#"alarm" = alarm,"hailfinder" = hailfinder,"insurance" = insurance)
# vDatasets["alarm"] <- alarm
# vDatasets["hailfinder"] <- hailfinder
# vDatasets["insurance"] <- insurance

# print(vDatasets)
print("#################################################")
print("##Score Based Algorithms#########################")
print("#################################################")
if(identical(args[1], "alarm")){
data(alarm)
input = alarm
print("Using Alarm Dataset")
}else if(identical(args[1], "insurance")){
  data(insurance)
  input = insurance
  print("Using insurance Dataset")
}else if(identical(args[1], "hailfinder")){
  data(hailfinder)
  input = hailfinder
  print("Using hailfinder Dataset")
}else if(identical(args[1], "asia")){
  data(asia)
  input = asia
  print("Using asia Dataset")
}else{
  data(alarm)
  input = alarm
  print("Defaults: Using Alarm Dataset")
}

##################################################################################################
##Execute different algorithms
  #### Score based algorithm
  bn.hc = hc(input, debug=debugFlag) #Hill Climbing algorithm
  
  #### Constraint Based Algorithm
  v_gs = gs(alarm, blacklist=NULL) ##Grow Shrink algorithm
  V_iamb  = iamb(input,blacklist=NULL) ##c("FIO2","PVS") Incremental Association Marklov Blanket (IAMB) algorithmi
  v_fiamb = fast.iamb(input,blacklist=NULL) ##Fast Incremental Association Marklov Blanket (Fast-IAMB) algorithm
  v_iiamb = inter.iamb(input,blacklist=NULL) ##Interleaved Incremental Association Marklov Blanket (Inter-IAMB) algorithm
  
  #### Hybrid Algorithm
  v_mmhc = mmhc(input, debug=debugFlag) ##Max-min hill climbing
  v_rsmax2 = rsmax2(input) ##Two-Phase Restricted Maximization (RSMAX2) algorithm
  
  #### Local Discovery algorithm
  v_cl = chow.liu(input, debug=debugFlag) ##Chow Liu algorithm

  sink()
##################################################################################################


##################################################################################################
##Compare results for each algorithms
print("Comparision between HC and self:")
print(shd(bn.hc,bn.hc, debug = debugFlag))
print(unlist(compare(bn.hc,bn.hc)) )
print(compare(bn.hc,bn.hc,arcs = debugFlag)) 

print("Comparision between HC and GS:")
print(shd(bn.hc,v_gs, debug = debugFlag))
print(unlist(compare(bn.hc,v_gs)))
print(compare(bn.hc,v_gs,arcs = debugFlag)) 

print("Comparision between HC and IAMB:")
print(shd(bn.hc,V_iamb, debug = debugFlag))
print(unlist(compare(bn.hc,V_iamb)))
print(compare(bn.hc,V_iamb,arcs = debugFlag)) 

print("Comparision between HC and Fast-IAMB:")
print(shd(bn.hc,v_fiamb, debug = debugFlag))
print(unlist(compare(bn.hc,v_fiamb)))
print(compare(bn.hc,v_fiamb,arcs = debugFlag)) 

print("Comparision between HC and Inter-IAMB:")
print(shd(bn.hc,v_iiamb, debug = debugFlag))
print(unlist(compare(bn.hc,v_iiamb)))
print(compare(bn.hc,v_iiamb,arcs = debugFlag)) 

print("Comparision between HC and  MMHC:")
print(shd(bn.hc,v_mmhc, debug = debugFlag))
print(unlist(compare(bn.hc,v_mmhc)))
print(compare(bn.hc,v_mmhc,arcs = debugFlag)) 

print("Comparision between HC and RSMAX2:")
print(shd(bn.hc,v_rsmax2, debug = debugFlag))
print(unlist(compare(bn.hc,v_rsmax2)))
print(compare(bn.hc,v_rsmax2,arcs = debugFlag)) 

print("Comparision between HC and Chow Liu:")
print(shd(bn.hc,v_cl, debug = debugFlag))
print(unlist(compare(bn.hc,v_cl)))
print(compare(bn.hc,v_cl,arcs = debugFlag)) 


sink()
##################################################################################################

##################################################################################################
##Get execution time for different algorithms
print("execution time for HC:")
print(system.time(hc(input)))

print("execution time for GC:")
print(system.time( gc(input)))

print("execution time for IAMB:")
print(system.time(iamb(input)))

print("execution time for Fast-IAMB:")
print(system.time(fast.iamb(input)))

print("execution time for Inter-IAMB:")
print(system.time(inter.iamb(input)) )

print("execution time for MMHC:")
print(system.time(mmhc(input)) )

print("execution time for RSMAX2:")
print(system.time(rsmax2(input)))

print("execution time for Chow Liu:")
print(system.time(chow.liu(input)))

sink()
##################################################################################################

##################################################################################################
##Get Scores for different algorithms

print("LogLik Score for HC:")
print(score(bn.hc,input,type="loglik"))

print("LogLik  Score for GC:")
print(score(v_gs,input,type="loglik"))

print("LogLik Score for IAMB:")
print(score(v_iamb,input,type="loglik"))

print("LogLik Score for Fast-IAMB:")
print(score(v_fiamb,input,type="loglik"))

print("LogLik Score for Inter-IAMB:")
print(score(v_iiamb,input,type="loglik"))

print("LogLik Score for MMHC:")
print(score(v_mmhc,input,type="loglik"))

print("LogLik Score for RSMAX2:")
print(score(v_rsmax2,input,type="loglik"))

print("LogLik Score for Chow Liu:")
print(score(v_cl,input,type="loglik"))  

sink()


  print("K2 Score for HC:")
print(score(bn.hc,input,type="k2"))

print("K2  Score for GC:")
print(score(v_gs,input,type="k2"))

print("K2 Score for IAMB:")
print(score(v_iamb,input,type="k2"))

print("K2 Score for Fast-IAMB:")
print(score(v_fiamb,input,type="k2"))

print("K2 Score for Inter-IAMB:")
print(score(v_iiamb,input,type="k2"))

print("K2 Score for MMHC:")
print(score(v_mmhc,input,type="k2"))

print("K2 Score for RSMAX2:")
print(score(v_rsmax2,input,type="k2"))

print("K2 Score for Chow Liu:")
print(score(v_cl,input,type="k2"))  

sink()
##################################################################################################
##Draw graph for different algorithms
par(mfrow = c(1,2), omi = rep(0, 4), mar = c(1, 0, 1, 0))
graphviz.plot(bn.hc,main = "Hill-Climbing algorithm")

par(mfrow = c(1,2), omi = rep(0, 4), mar = c(1, 0, 1, 0))
graphviz.plot(v_gs,main = "Grow-Shrink algorithm")

par(mfrow = c(1,2), omi = rep(0, 4), mar = c(1, 0, 1, 0))
graphviz.plot(V_iamb,main = "IAMB algorithm")

par(mfrow = c(1,2), omi = rep(0, 4), mar = c(1, 0, 1, 0))
graphviz.plot(v_fiamb,main = "FIAMB algorithm")

par(mfrow = c(1,2), omi = rep(0, 4), mar = c(1, 0, 1, 0))
graphviz.plot(v_iiamb,main = "IIAMB algorithm")

par(mfrow = c(1,2), omi = rep(0, 4), mar = c(1, 0, 1, 0))
graphviz.plot(v_mmhc,main = "MMHC algorithm")

par(mfrow = c(1,2), omi = rep(0, 4), mar = c(1, 0, 1, 0))
graphviz.plot(v_rsmax2,main = "RSMAX2 algorithm")

par(mfrow = c(1,2), omi = rep(0, 4), mar = c(1, 0, 1, 0))
graphviz.plot(v_cl,main = "Chow-Liu algorithm")

sink()



