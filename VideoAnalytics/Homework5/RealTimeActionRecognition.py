__author__ = 'Arunkumar'
import numpy as np
import cv2
import video
from common import nothing, clock, draw_str
import os, sys
import math
from collections import defaultdict
from matplotlib import pyplot as plt

##Set SVM params
SZ=20
bin_n = 16 # Number of bins
svm_params = dict( kernel_type = cv2.SVM_RBF,
                    svm_type = cv2.SVM_C_SVC,
                    C=2.67, gamma=5.383) ## gamma=0.50625000000000009; C=312.50000000000000 term_crit=cvTermCriteria(CV_TERMCRIT_ITER,100,0.000001)
                    # C=2.67, gamma=5.383 ## C=312.50000000000000, gamma=0.50625000000000009
affine_flags = cv2.WARP_INVERSE_MAP|cv2.INTER_LINEAR

##declare other variables
MHI_DURATION =0.2
DEFAULT_THRESHOLD = 40
MAX_TIME_DELTA = 0.5
MIN_TIME_DELTA = 0.05

gSkipFrames = 12
version = 'RT_skip'+str(gSkipFrames)#'5'

lstLabels = ['walk','run','jump','side','bend','wave1','wave2','pjump','jack','skip']
iLabels = {1:'walk',2:'run',3:'jump',4:'side',5:'bend',6:'wave1',7:'wave2',8:'pjump',9:'jack',10:'skip'}
sLabels = {'walk':1,'run':2,'jump':3,'side':4,'bend':5,'wave1':6,'wave2':7,'pjump':8,'jack':9,'skip':10}
visuals = ['input', 'frame_diff', 'motion_hist', 'grad_orient']

readTrainFeaturesFromFile = False
readTestFeaturesFromFile = False
trainFeatureFileName = "trainFeatures"
testFeatureFileName = "testFeatures"

groupedTrainFiles = defaultdict(list)
groupedTestFiles = defaultdict(list)

sDetector = "SURF"
sExtractor = "SURF"
sDescriptor = "FlannBased"

matcher = cv2.DescriptorMatcher_create(sDescriptor)
extractor = cv2.DescriptorExtractor_create(sExtractor)
detector = cv2.FeatureDetector_create(sDetector)

#Kernel for image manipulation
kernel = np.ones((5,5),np.uint8)

dictSize = 1000
retries = 1
flags = cv2.KMEANS_PP_CENTERS

# int dictionarySize = 1000;
# TermCriteria tc(CV_TERMCRIT_ITER, 10, 0.001);
# int retries = 1;
# int flags = KMEANS_PP_CENTERS;

bowTrainer = cv2.BOWKMeansTrainer(clusterCount=dictSize,termcrit=(cv2.cv.CV_TERMCRIT_ITER, 10, 0.001),attempts=retries,flags=flags)
bowDE = cv2.BOWImgDescriptorExtractor(extractor,matcher)

trainData = [] #np.float32( np.zeros((0, 128))) #cv2.cv.CreateMat(0,1000,cv2.cv.CV_32FC1) #
trainLabels = [] #np.float32(np.empty((0, 1), np.float32)) #np.empty((0, 1), np.float32)

classifier = cv2.SVM()

testData = [] #np.zeros((0, 1), np.float32)
testLabels = [] #np.zeros((0, 1), np.float32)

#For saveing training state for future
onlyTest = True
testRealTime = True
saveTrainFeaturesDes = []



##########################################END DECLARATION OF VARIABLES ################################################

'''
Function to draw motion components on the MHI images
'''
def draw_motion_comp(vis, (x, y, w, h), angle, color):
    cv2.rectangle(vis, (x, y), (x+w, y+h), (0, 255, 0))
    r = min(w/2, h/2)
    cx, cy = x+w/2, y+h/2
    angle = angle*np.pi/180
    cv2.circle(vis, (cx, cy), r, color, 3)
    cv2.line(vis, (cx, cy), (int(cx+np.cos(angle)*r), int(cy+np.sin(angle)*r)), color, 3)


'''
##Function to create training and test files to store training/test labels and URLS
'''
def createTrainTestFiles(rootDir):
    dTree = [x[0] for x in os.walk(rootDir)]
    print([x[0] for x in os.walk(rootDir)])
    trainFilesDict = defaultdict(list)
    testFilesDict = defaultdict(list)
    outTrainFile = open('conf/trainFileList', 'wb+')
    outTestFile = open('conf/testFileList', 'wb+')

    for root,subdir,files in os.walk(rootDir):
        if root != rootDir:
            trainIdx = int(len(files) * 0.8)
            dirName = root.split("\\")[1]
            curLabel = lstLabels.index(dirName) + 1
            for f in files[:trainIdx]:
                trainFilesDict[curLabel].append(root+"\\"+f)
            for f in files[trainIdx:]:
                testFilesDict[curLabel].append(root+"\\"+f)
            print(curLabel,'::',trainFilesDict.get(curLabel),'::',testFilesDict.get(curLabel))
            outTrainFile.write(str(curLabel) + "\t"+' '.join(trainFilesDict.get(curLabel))+'\n')
            outTestFile.write(str(curLabel) + "\t"+' '.join(testFilesDict.get(curLabel))+'\n')
    outTestFile.close()
    outTrainFile.close()


'''
##Function to read Training and testing Files
'''
#read Training and Test File list
def readTrainTestFileList(pTrainFile=None,pTestFile=None):
    if(pTrainFile is None):
        pTrainFile='conf/trainFileList'
    if(pTestFile is None):
        pTestFile='conf/testFileList'
    # print(pTrainFile)

    print('\n\nReading Train File list..')
    with open(pTrainFile,'rU') as trainFile:
        for line in trainFile.readlines():

            keyVals = line.strip().split("\t")
            print(int(keyVals[0]),keyVals[1].split())
            for fileName in keyVals[1].split():
                groupedTrainFiles[int(keyVals[0])].append(fileName)

    print('\n\nReading Test File list..')
    with open(pTestFile,'rU') as testFile:
        for line in testFile.readlines():
            keyVals = line.strip().split()
            print(keyVals)
            print(int(keyVals[0]),keyVals[1].split())
            for fileName in keyVals[1].split():
                groupedTestFiles[int(keyVals[0])].append(fileName)
    # print(groupedTestFiles)
'''
##Function for formating print output
'''
def printPageBreak():
    print("\n======================================================================================================\n")


#####################################################################################################################
############################ Function to process training and test feature files ####################################
#####################################################################################################################

def processTrainingFiles(oper="extractTrainingVocabulary",fileType="train"):
    global saveTrainFeaturesDes, visuals, gSkipFrames

    cv2.namedWindow('Output')
    cv2.createTrackbar('visual', 'Output', 2, len(visuals)-1, nothing)
    cv2.createTrackbar('threshold', 'Output', DEFAULT_THRESHOLD, 255, nothing)

    printPageBreak()
    print("\n\nProcessing "+fileType.capitalize()+"ing Files")
    trainTestFiles = groupedTrainFiles
    if fileType == "train":
        trainTestFiles = groupedTrainFiles
    else:
        trainTestFiles = groupedTestFiles

    for label, fileList in trainTestFiles.iteritems():
        global trainData, testData, trainLabels, testLabels
        for file in fileList:
            frameCounter = 0
            print(label,file)
            cam = cv2.VideoCapture(file)

            ret, frame = cam.read()
            h, w = frame.shape[:2]
            prev_frame = frame.copy()
            motion_history = np.zeros((h, w), np.float32)
            hsv = np.zeros((h, w, 3), np.uint8)
            hsv[:,:,1] = 255
            totalFrames = int(cam.get(cv2.cv.CV_CAP_PROP_FRAME_COUNT))
            frameCounter = frameCounter +1
            print(totalFrames)
            subFrameCount = 0
            skipFrames = gSkipFrames

            ##Read all frames
            while(frameCounter < totalFrames):
                ret, frame = cam.read()
                if(subFrameCount == 0):
                    frame_diff = cv2.absdiff(frame, prev_frame)
                    prev_frame = frame.copy()
                    gray_diff = cv2.cvtColor(frame_diff, cv2.COLOR_BGR2GRAY)
                    thrs = cv2.getTrackbarPos('threshold', 'Output')
                    ret, motion_mask = cv2.threshold(gray_diff, thrs, 1, cv2.THRESH_BINARY)
                    timestamp = clock()
                    cv2.updateMotionHistory(motion_mask, motion_history, timestamp, MHI_DURATION)

                    mg_mask, mg_orient = cv2.calcMotionGradient( motion_history, MAX_TIME_DELTA, MIN_TIME_DELTA, apertureSize=5 )
                    seg_mask, seg_bounds = cv2.segmentMotion(motion_history, timestamp, MAX_TIME_DELTA)

                    visual_name = visuals[cv2.getTrackbarPos('visual', 'Output')]
                    if visual_name == 'input':
                        vis = frame.copy()
                    elif visual_name == 'frame_diff':
                        vis = frame_diff.copy()
                    elif visual_name == 'motion_hist':
                        vis = np.uint8(np.clip((motion_history-(timestamp-MHI_DURATION)) / MHI_DURATION, 0, 1)*255)
                        vis = cv2.cvtColor(vis, cv2.COLOR_GRAY2BGR)
                    elif visual_name == 'grad_orient':
                        hsv[:,:,0] = mg_orient/2
                        hsv[:,:,2] = mg_mask*255
                        vis = cv2.cvtColor(hsv, cv2.COLOR_HSV2BGR)

                    for i, rect in enumerate([(0, 0, w, h)] + list(seg_bounds)):
                        x, y, rw, rh = rect
                        area = rw*rh
                        if area < 64**2:
                            continue
                        silh_roi   = motion_mask   [y:y+rh,x:x+rw]
                        orient_roi = mg_orient     [y:y+rh,x:x+rw]
                        mask_roi   = mg_mask       [y:y+rh,x:x+rw]
                        mhi_roi    = motion_history[y:y+rh,x:x+rw]
                        if cv2.norm(silh_roi, cv2.NORM_L1) < area*0.05:
                            continue
                        angle = cv2.calcGlobalOrientation(orient_roi, mask_roi, mhi_roi, timestamp, MHI_DURATION)
                        color = ((255, 0, 0), (0, 0, 255))[i == 0]
                        draw_motion_comp(vis, rect, angle, color)

                    visCopy = vis.copy()
                    draw_str(visCopy, (20, 20), visual_name)
                    cv2.imshow('Output', visCopy)

                subFrameCount = subFrameCount + 1
                if(subFrameCount > skipFrames):
                    subFrameCount = 0
                frameCounter = frameCounter + 1

                if(visual_name == 'motion_hist'):
                    mhi_vis = np.uint8(np.clip((motion_history-(timestamp-MHI_DURATION)) / MHI_DURATION, 0, 1)*255)
                    mhi_vis = cv2.cvtColor(mhi_vis, cv2.COLOR_GRAY2BGR)
                else:
                    hsv[:,:,0] = mg_orient/2
                    hsv[:,:,2] = mg_mask*255
                    mhi_vis = cv2.cvtColor(hsv, cv2.COLOR_HSV2BGR)
                ##Start extracting features
                sift = cv2.SIFT()
                denseDetector = cv2.FeatureDetector_create(sDetector) ##using Dense Feature Detector

                kp = detector.detect(mhi_vis)
                print('KeyPoints Length:: ',len(kp))

                ##Check if there are any detected keypoints before processing.
                if len(kp) > 0:
                    features = extractor.compute(mhi_vis,kp)
                    featuresDes = features[1]
                    print('Descriptors:: ',featuresDes)
                    print('Descriptors Length:: ',len(featuresDes))
                    print('Descriptors Shape:: ',featuresDes.shape)

                    # print('KeyPoints:: ',kp)
                    # print('Descriptors:: ',des)
                    #desFlattened = features.flatten()
                    # print('desFlattened Length:: ',len(desFlattened)
                    if(oper == "extractTrainingVocabulary"):
                        bowTrainer.add(featuresDes)
                        saveTrainFeaturesDes.append(featuresDes)
                        cv2.imwrite('mhiImages\\'+file.split("\\")[2]+'_mhi.jpg',mhi_vis)
                        # saveTrainFeaturesDes1 = np.array(saveTrainFeaturesDes)
                        # saveTrainFeaturesDes1.dump(open('trainFeatures.dat', 'wb'))
                    elif(oper=="extractBOWDescriptor"):
                        bowDescriptor = bowDE.compute(mhi_vis, kp)
                        # descriptors.push_back(bowDescriptor);
                        # print('bowDescriptor:: ',bowDescriptor)
                        print('bowDescriptor Length:: ',len(bowDescriptor))
                        print('bowDescriptor Shape:: ',bowDescriptor.shape)

                        ##Check if the operation on training data or test data
                        if(fileType=="train"):
                            trainData.append(bowDescriptor)
                            trainLabels.append(label)
                            img=cv2.drawKeypoints(mhi_vis,kp)
                            cv2.imwrite('keyPoints\\'+file.split("\\")[2]+'_keypoints.jpg',img)
                        else:
                            testData.append(bowDescriptor)
                            testLabels.append(label)
                else:
                    print("No Key points were detectected for this image..")

                print(file.split("\\")[2])

                # cv2.imwrite('keyPoints\\'+file.split("\\")[2]+'_keypoints.jpg',img)
                cv2.waitKey(25)

    cv2.destroyAllWindows()

#####################################################################################################################
########################## End of Function to process training and test feature files ###############################
#####################################################################################################################

#####################################################################################################################
########################## Start  Function to process real time test video ##########################################
#####################################################################################################################
def doTestArchive():
    global saveTrainFeaturesDes, visuals, gSkipFrames, testData,testLabels

    print("Testing on real time video")
    cv2.namedWindow('Output')
    cv2.createTrackbar('visual', 'Output', 2, len(visuals)-1, nothing)
    cv2.createTrackbar('threshold', 'Output', DEFAULT_THRESHOLD, 255, nothing)
    testData = []
    testLabels = []

    printPageBreak()
    print("\n\nProcessing real time video..")

    frameCounter = 0
    cam = cv2.VideoCapture(0)

    ret, frame = cam.read()
    h, w = frame.shape[:2]
    prev_frame = frame.copy()
    motion_history = np.zeros((h, w), np.float32)
    hsv = np.zeros((h, w, 3), np.uint8)
    hsv[:,:,1] = 255
    frameCounter = frameCounter +1
    subFrameCount = 0
    skipFrames = gSkipFrames

    label = ""

    ##Read all frames
    while(True):
        ret, frame = cam.read()
        if(subFrameCount == 0):
            frame_diff = cv2.absdiff(frame, prev_frame)
            prev_frame = frame.copy()
            gray_diff = cv2.cvtColor(frame_diff, cv2.COLOR_BGR2GRAY)
            thrs = cv2.getTrackbarPos('threshold', 'Output')
            ret, motion_mask = cv2.threshold(gray_diff, thrs, 1, cv2.THRESH_BINARY)
            timestamp = clock()
            cv2.updateMotionHistory(motion_mask, motion_history, timestamp, MHI_DURATION)

            mg_mask, mg_orient = cv2.calcMotionGradient( motion_history, MAX_TIME_DELTA, MIN_TIME_DELTA, apertureSize=5 )
            seg_mask, seg_bounds = cv2.segmentMotion(motion_history, timestamp, MAX_TIME_DELTA)

            visual_name = visuals[cv2.getTrackbarPos('visual', 'Output')]
            if visual_name == 'input':
                vis = frame.copy()
            elif visual_name == 'frame_diff':
                vis = frame_diff.copy()
            elif visual_name == 'motion_hist':
                vis = np.uint8(np.clip((motion_history-(timestamp-MHI_DURATION)) / MHI_DURATION, 0, 1)*255)
                vis = cv2.cvtColor(vis, cv2.COLOR_GRAY2BGR)
            elif visual_name == 'grad_orient':
                hsv[:,:,0] = mg_orient/2
                hsv[:,:,2] = mg_mask*255
                vis = cv2.cvtColor(hsv, cv2.COLOR_HSV2BGR)

            for i, rect in enumerate([(0, 0, w, h)] + list(seg_bounds)):
                x, y, rw, rh = rect
                area = rw*rh
                if area < 64**2:
                    continue
                silh_roi   = motion_mask   [y:y+rh,x:x+rw]
                orient_roi = mg_orient     [y:y+rh,x:x+rw]
                mask_roi   = mg_mask       [y:y+rh,x:x+rw]
                mhi_roi    = motion_history[y:y+rh,x:x+rw]
                if cv2.norm(silh_roi, cv2.NORM_L1) < area*0.05:
                    continue
                angle = cv2.calcGlobalOrientation(orient_roi, mask_roi, mhi_roi, timestamp, MHI_DURATION)
                color = ((255, 0, 0), (0, 0, 255))[i == 0]
                draw_motion_comp(vis, rect, angle, color)

            visCopy = vis.copy()
            draw_str(visCopy, (20, 20), visual_name)
            cv2.imshow('Output', visCopy)

        subFrameCount = subFrameCount + 1
        if(subFrameCount > skipFrames):
            subFrameCount = 0
        frameCounter = frameCounter + 1

        if(visual_name == 'motion_hist'):
            mhi_vis = np.uint8(np.clip((motion_history-(timestamp-MHI_DURATION)) / MHI_DURATION, 0, 1)*255)
            mhi_vis = cv2.cvtColor(mhi_vis, cv2.COLOR_GRAY2BGR)
        else:
            hsv[:,:,0] = mg_orient/2
            hsv[:,:,2] = mg_mask*255
            mhi_vis = cv2.cvtColor(hsv, cv2.COLOR_HSV2BGR)
        ##Start extracting features
        sift = cv2.SIFT()
        denseDetector = cv2.FeatureDetector_create(sDetector) ##using Dense Feature Detector

        kp = detector.detect(mhi_vis)
        print('KeyPoints Length:: ',len(kp))

        ##Check if there are any detected keypoints before processing.
        if len(kp) > 0:
            features = extractor.compute(mhi_vis,kp)
            featuresDes = features[1]
            # print('Descriptors:: ',featuresDes)
            # print('featuresDes Length:: ',len(featuresDes))
            # print('featuresDes Shape:: ',featuresDes.shape)

            bowDescriptor = bowDE.compute(mhi_vis, kp)
            # descriptors.push_back(bowDescriptor);
            # print('bowDescriptor:: ',bowDescriptor)
            # print('bowDescriptor Length:: ',len(bowDescriptor))
            # print('bowDescriptor Shape:: ',bowDescriptor.shape)

            ##Check if the operation on training data or test data
            testData.append(bowDescriptor)
            testLabels.append(label)
        else:
            print("No Key points were detectected for this image..")

        if(subFrameCount == 0 or frameCounter > 30):
            testData = np.float32(testData).reshape(-1,dictSize)
            # testLabels = np.float32(testLabels).reshape(-1,1)
            print("testData Shape: ", testData.shape)
            # print("testLabels Shape: ", testLabels.shape)
            print("testData : ", testData)
            # print("testLabels : ", testLabels)

            result = classifier.predict_all(testData)

            #######   Check Accuracy   ########################
            printPageBreak()
            printPageBreak()

            # print("TestLabels: ", testLabels.reshape(1,-1))
            print("Results: ", result.reshape(1,-1));
            frameCounter = 0
            testData = []
            testLabels = []

        draw_str(frame, (20, 20), label)
        cv2.imshow('Output', visCopy)
        cv2.imshow('Input', frame)

        # cv2.imwrite('keyPoints\\'+file.split("\\")[2]+'_keypoints.jpg',img)
        cv2.waitKey(25)

    cv2.destroyAllWindows()





#####################################################################################################################
##########################  End Function to process real time test video ############################################
#####################################################################################################################

#####################################################################################################################
########################## Start  Function to process real time test video ##########################################
#####################################################################################################################
def doTestRealTime():
    global saveTrainFeaturesDes, visuals, gSkipFrames, testData,testLabels

    print("Testing on real time video")
    cv2.namedWindow('Output')
    cv2.createTrackbar('visual', 'Output', 2, len(visuals)-1, nothing)
    cv2.createTrackbar('threshold', 'Output', DEFAULT_THRESHOLD, 255, nothing)
    testData = []
    testLabels = []

    printPageBreak()
    print("\n\nProcessing real time video..")

    frameCounter = 0
    cam = cv2.VideoCapture(0)

    ret, frame = cam.read()
    h, w = frame.shape[:2]
    prev_frame = frame.copy()
    motion_history = np.zeros((h, w), np.float32)
    hsv = np.zeros((h, w, 3), np.uint8)
    hsv[:,:,1] = 255
    frameCounter = frameCounter +1
    subFrameCount = 0
    skipFrames = gSkipFrames

    label = ""

    ##Read all frames
    while(True):
        ret, frame = cam.read()
        if(subFrameCount == 0):
            frame_diff = cv2.absdiff(frame, prev_frame)
            prev_frame = frame.copy()
            gray_diff = cv2.cvtColor(frame_diff, cv2.COLOR_BGR2GRAY)
            thrs = cv2.getTrackbarPos('threshold', 'Output')
            ret, motion_mask = cv2.threshold(gray_diff, thrs, 1, cv2.THRESH_BINARY)
            timestamp = clock()
            cv2.updateMotionHistory(motion_mask, motion_history, timestamp, MHI_DURATION)

            mg_mask, mg_orient = cv2.calcMotionGradient( motion_history, MAX_TIME_DELTA, MIN_TIME_DELTA, apertureSize=5 )
            seg_mask, seg_bounds = cv2.segmentMotion(motion_history, timestamp, MAX_TIME_DELTA)

            visual_name = visuals[cv2.getTrackbarPos('visual', 'Output')]
            if visual_name == 'input':
                vis = frame.copy()
            elif visual_name == 'frame_diff':
                vis = frame_diff.copy()
            elif visual_name == 'motion_hist':
                vis = np.uint8(np.clip((motion_history-(timestamp-MHI_DURATION)) / MHI_DURATION, 0, 1)*255)
                vis = cv2.cvtColor(vis, cv2.COLOR_GRAY2BGR)
            elif visual_name == 'grad_orient':
                hsv[:,:,0] = mg_orient/2
                hsv[:,:,2] = mg_mask*255
                vis = cv2.cvtColor(hsv, cv2.COLOR_HSV2BGR)

            for i, rect in enumerate([(0, 0, w, h)] + list(seg_bounds)):
                x, y, rw, rh = rect
                area = rw*rh
                if area < 64**2:
                    continue
                silh_roi   = motion_mask   [y:y+rh,x:x+rw]
                orient_roi = mg_orient     [y:y+rh,x:x+rw]
                mask_roi   = mg_mask       [y:y+rh,x:x+rw]
                mhi_roi    = motion_history[y:y+rh,x:x+rw]
                if cv2.norm(silh_roi, cv2.NORM_L1) < area*0.05:
                    continue
                angle = cv2.calcGlobalOrientation(orient_roi, mask_roi, mhi_roi, timestamp, MHI_DURATION)
                color = ((255, 0, 0), (0, 0, 255))[i == 0]
                draw_motion_comp(vis, rect, angle, color)

            visCopy = vis.copy()
            draw_str(visCopy, (20, 20), visual_name)
            cv2.imshow('Output', visCopy)

        if(visual_name == 'motion_hist'):
            mhi_vis = np.uint8(np.clip((motion_history-(timestamp-MHI_DURATION)) / MHI_DURATION, 0, 1)*255)
            mhi_vis = cv2.cvtColor(mhi_vis, cv2.COLOR_GRAY2BGR)
        else:
            hsv[:,:,0] = mg_orient/2
            hsv[:,:,2] = mg_mask*255
            mhi_vis = cv2.cvtColor(hsv, cv2.COLOR_HSV2BGR)
        ##Start extracting features
        sift = cv2.SIFT()
        denseDetector = cv2.FeatureDetector_create(sDetector) ##using Dense Feature Detector

        kp = detector.detect(mhi_vis)
        print('KeyPoints Length:: ',len(kp))

        ##Check if there are any detected keypoints before processing.
        if(subFrameCount == 0):
            if len(kp) > 0:
                features = extractor.compute(mhi_vis,kp)
                featuresDes = features[1]
                # print('Descriptors:: ',featuresDes)
                # print('featuresDes Length:: ',len(featuresDes))
                # print('featuresDes Shape:: ',featuresDes.shape)

                bowDescriptor = bowDE.compute(mhi_vis, kp)
                # descriptors.push_back(bowDescriptor);
                # print('bowDescriptor:: ',bowDescriptor)
                # print('bowDescriptor Length:: ',len(bowDescriptor))
                # print('bowDescriptor Shape:: ',bowDescriptor.shape)

                ##Check if the operation on training data or test data
                testData.append(bowDescriptor)
                testLabels.append(label)
            else:
                print("No Key points were detectected for this image..")

        if(subFrameCount == 0):# or frameCounter > 30):
            testData = np.float32(testData).reshape(-1,dictSize)
            # testLabels = np.float32(testLabels).reshape(-1,1)
            print("testData Shape: ", testData.shape)
            # print("testLabels Shape: ", testLabels.shape)
            print("testData : ", testData)
            # print("testLabels : ", testLabels)

            result = classifier.predict_all(testData)

            #######   Check Accuracy   ########################
            printPageBreak()
            printPageBreak()

            # print("TestLabels: ", testLabels.reshape(1,-1))
            print("Results: ", result.reshape(1,-1));
            frameCounter = 0
            testData = []
            testLabels = []

        draw_str(frame, (20, 20), label)
        cv2.imshow('Output', visCopy)
        cv2.imshow('Input', frame)
        # cv2.imwrite('keyPoints\\'+file.split("\\")[2]+'_keypoints.jpg',img)
        cv2.waitKey(25)

        ##Update counters
        subFrameCount = subFrameCount + 1
        if(subFrameCount > skipFrames):
            subFrameCount = 0
        frameCounter = frameCounter + 1




    cv2.destroyAllWindows()





#####################################################################################################################
##########################  End Function to process real time test video ############################################
#####################################################################################################################


'''
##Main Function
'''
if __name__ == '__main__':
    global trainData,trainLabels,testLabels,testData,classifier,saveTrainFeaturesDes

    print("What do you want to do?\n1. Train\n 2. Test")
    print 'enter "q" at any stage to quit.'
    # sel = raw_input('Type 1 to Train data or 2 to Test: ')
    createTrainTestFiles('dataset-1') #Split the dataset
    readTrainTestFileList()

    if not(onlyTest):
        processTrainingFiles()
        print("Saving trained Features to trainFeatures.dat..")
        #@Save Training features
        saveTrainFeaturesDes1 = np.array(saveTrainFeaturesDes)
        saveTrainFeaturesDes1.dump(open('conf/trainFeatures_v'+version+'.dat', 'wb'))

        ##Get BOW descriptors
        descriptors = bowTrainer.getDescriptors();
        print("Clustering ",len(descriptors)," Features..")
        dictionary = bowTrainer.cluster();
        bowDE.setVocabulary(dictionary);

        ######     Now training      ########################
        print("Processing Training Data..")
        trainData = []
        trainLabels = []

        processTrainingFiles(oper="extractBOWDescriptor")

        trainData = np.float32(trainData).reshape(-1,dictSize)
        trainLabels = np.float32(trainLabels).reshape(-1,1)

        print("TrainingData Shape: ", trainData.shape)
        print("TrainingLabel Shape: ", trainLabels.shape)
        print("TrainingData : ", trainData)
        print("TrainingLabel : ", trainLabels)

        print("Training classifier..")
        classifier.train(trainData, trainLabels, params=svm_params)
        classifier.save('conf/svm_data_v'+version+'.dat')
    else:
        saveTrainFeaturesDes1 = np.load(open('conf/trainFeatures_v'+version+'.dat', 'rb'))
        for savedFeatures in saveTrainFeaturesDes1:
            bowTrainer.add(savedFeatures)
        ##Get BOW descriptors
        descriptors = bowTrainer.getDescriptors()
        print("Clustering ",len(descriptors)," Features..")
        dictionary = bowTrainer.cluster()
        bowDE.setVocabulary(dictionary)
        classifier.load('conf/svm_data_v'+version+'.dat')


    if(testRealTime):
        doTestRealTime()