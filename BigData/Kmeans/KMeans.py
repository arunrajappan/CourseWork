__author__ = 'Arunkumar'
#import all the libraries
import sys
import math

kVal = 3
clusters = []
means = [[2,10],[5,8],[1,2]]
#: (2,10); (2,5); (8,4); (5,8); (7,5); (6,4); (1,2); (4,9)'
mypoints = list([[2,10],[2,5],[8,4],[5,8],[7,5],[6,4],[1,2],[4,9]])

#this is my main funcition'
def main(args):
    print(mypoints[0])
    setupClusters()

def setupClusters():
    for ik in range(1,kVal+1):
        clusters.append([])
        #clusters[ik] = list([])
    for ic in clusters:
        print(ic)

    isConverged = False
    loopCount = 0
    while(isConverged == False):
        clusters.clear()
        for ik in range(1,kVal+1):
            clusters.append([])
        curPointIndx = 0
        print('Current Centroids are ',means)
        ##assign clusters
        for points in mypoints:
            euDist = list()
            minDistIndx = 0
            belongsToCluster = 0
            curIdx = 0

            ## Get distance of current point from all the centroids
            for centroid in means:
                vDist = math.sqrt(math.pow(points[0]-centroid[0],2) + math.pow(points[1]-centroid[1],2))
                euDist.append(vDist)
                if(euDist.__len__() > 1 and euDist[belongsToCluster]>euDist[curIdx]):
                    belongsToCluster = curIdx
                curIdx = curIdx + 1;
            clusters[belongsToCluster].append(curPointIndx)
            curPointIndx = curPointIndx + 1
            print(points,' || ',euDist,' || assigned cluster = ',belongsToCluster )
        print('Current Clusters are: ',clusters)

        curCentroidIdx = 0
        isChanged = False

        ## Calculate new Centroids ##
        for cgroup in clusters:
            if(cgroup.__len__() >0):
                sumX = 0.0
                sumY = 0.0
                for point in cgroup:
                    sumX = sumX + mypoints[point][0]
                    sumY = sumY + mypoints[point][1]

                sumX = sumX/cgroup.__len__()
                sumY = sumY/cgroup.__len__()
                if(means[curCentroidIdx][0] != sumX or means[curCentroidIdx][1] != sumY):
                    isChanged = True
                    means[curCentroidIdx][0] = sumX
                    means[curCentroidIdx][1] = sumY
                curCentroidIdx = curCentroidIdx +1
        loopCount = loopCount + 1
        ## Check if it has converged
        if(isChanged == False or loopCount == 150):
            isConverged = True
        print('\n\n==================================================================================================')

    """""""""""""""""""""""""""""""""""
    ###### Print all the Outputs ######
    """""""""""""""""""""""""""""""""""
    print('Final Centroids are: ',means)
    print('Final points are: ', mypoints)
    print('Final Clusters are: ', clusters)
    output = '{'
    cCount = 0
    for cluster in clusters:
        output = output + '['
        count = 0
        for point in cluster:
            output = output + mypoints[point].__str__()
            count = count +1
            if(count < cluster.__len__()):
                output = output + ','
        output = output + ']'
        cCount = cCount + 1
        if(cCount < clusters.__len__()):
            output = output + ' , '
    output = output + '}'
    print('Final Clusters (with points) are: ', output)

#this is my entry function for the code
if __name__ == "__main__":
   main(sys.argv[1:])
