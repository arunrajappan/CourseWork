__author__ = 'Arunkumar'

import numpy as np
import cv2
import video
from common import nothing, clock, draw_str
import os, sys
import math
from collections import defaultdict

##Set SVM params
SZ=20
bin_n = 16 # Number of bins
svm_params = dict( kernel_type = cv2.SVM_RBF,
                    svm_type = cv2.SVM_C_SVC,
                    C=2.67, gamma=5.383) ## gamma=0.50625000000000009; C=312.50000000000000 term_crit=cvTermCriteria(CV_TERMCRIT_ITER,100,0.000001)
                    # C=2.67, gamma=5.383 ## C=312.50000000000000, gamma=0.50625000000000009
affine_flags = cv2.WARP_INVERSE_MAP|cv2.INTER_LINEAR

##declare other variables
MHI_DURATION = 1.0
DEFAULT_THRESHOLD = 32
MAX_TIME_DELTA = 0.5
MIN_TIME_DELTA = 0.05

version = '11_sum'

lstLabels = ['walk','run','jump','side','bend','wave1','wave2','pjump','jack','skip']
iLabels = {1:'walk',2:'run',3:'jump',4:'side',5:'bend',6:'wave1',7:'wave2',8:'pjump',9:'jack',10:'skip'}
sLabels = {'walk':1,'run':2,'jump':3,'side':4,'bend':5,'wave1':6,'wave2':7,'pjump':8,'jack':9,'skip':10}

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
featureSize = 64

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
    outTrainFile = open('trainFileList', 'wb+')
    outTestFile = open('testFileList', 'wb+')

    for root,subdir,files in os.walk(rootDir):
        if root != rootDir:
            trainIdx = int(len(files) * 1.0)
            print(root)
            # root = root.replace("\\",'/')
            # print("New Root - ",root)
            dirName = root.split("\\")[1]
            curLabel = lstLabels.index(dirName) + 1
            for f in files[:trainIdx]:
                trainFilesDict[curLabel].append(root+"\\"+f)
            for f in files[trainIdx:]:
                testFilesDict[curLabel].append(root+"\\"+f)
            print(curLabel,'::',trainFilesDict.get(curLabel),'::',testFilesDict.get(curLabel))
            outTrainFile.write(str(curLabel) + "\t"+' '.join(trainFilesDict.get(curLabel))+'\n')
            if(testFilesDict.get(curLabel) != None):
                outTestFile.write(str(curLabel) + "\t"+' '.join(''+testFilesDict.get(curLabel))+'\n')
    outTestFile.close()
    outTrainFile.close()

'''
##Function to read Training and testing Files
'''
#read Training and Test File list
def readTrainTestFileList(pTrainFile=None,pTestFile=None):
    if(pTrainFile is None):
        pTrainFile='trainFileList'
    if(pTestFile is None):
        pTestFile='testFileList'
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
            print(int(keyVals[0]),keyVals[1].split())
            for fileName in keyVals[1].split():
                groupedTestFiles[int(keyVals[0])].append(fileName)
    # print(groupedTestFiles)
'''
##Function for formating print output
'''
def printPageBreak():
    print("\n======================================================================================================\n")


'''
##Function to process training and test feature files
'''
def processTrainingFiles(oper="extractTrainingVocabulary",fileType="train"):
    global saveTrainFeaturesDes
    cv2.namedWindow('Output')
    visuals = ['input', 'frame_diff', 'motion_hist', 'grad_orient']
    cv2.createTrackbar('visual', 'Output', 2, len(visuals)-1, nothing)
    cv2.createTrackbar('threshold', 'Output', DEFAULT_THRESHOLD, 255, nothing)

    cv2.namedWindow('Output7')
    # visuals = ['input', 'frame_diff', 'motion_hist', 'grad_orient']
    cv2.createTrackbar('visual', 'Output7', 2, len(visuals)-1, nothing)
    cv2.createTrackbar('threshold', 'Output7', DEFAULT_THRESHOLD, 255, nothing)

    cv2.namedWindow('Output14')
    # visuals = ['input', 'frame_diff', 'motion_hist', 'grad_orient']
    cv2.createTrackbar('visual', 'Output14', 2, len(visuals)-1, nothing)
    cv2.createTrackbar('threshold', 'Output14', DEFAULT_THRESHOLD, 255, nothing)


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
            # cam = video.create_capture(file,fallback='synth:class=chess:bg=lena.jpg:noise=0.01')
            # cam = video.create_capture(file, fallback='synth:class=chess:bg=lena.jpg:noise=0.01')
            ret, frame = cam.read()
            h, w = frame.shape[:2]
            prev_frame = frame.copy()
            motion_history = np.zeros((h, w), np.float32)
            # print(motion_history)
            hsv = np.zeros((h, w, 3), np.uint8)
            hsv[:,:,1] = 255

            prev_frame7 = frame.copy()
            motion_history7 = np.zeros((h, w), np.float32)
            # print(motion_history7)
            hsv7 = np.zeros((h, w, 3), np.uint8)
            hsv7[:,:,1] = 255

            prev_frame14 = frame.copy()
            motion_history14 = np.zeros((h, w), np.float32)
            # print(motion_history)
            hsv14 = np.zeros((h, w, 3), np.uint8)
            hsv14[:,:,1] = 255

            totalFrames = int(cam.get(cv2.cv.CV_CAP_PROP_FRAME_COUNT))
            frameCounter = frameCounter +1
            print(totalFrames)

            subFrameCount = 0
            skipFrames = 1

            subFrameCount7 = 0
            skipFrames7 = 7

            subFrameCount14 = 0
            skipFrames14 = 14

            featureDesSAHMIS = None
            bowDescriptorSAHMIS = None


            while(frameCounter < totalFrames):
                ret, frame = cam.read()

                '''
                #################################################################################SAMHI-1
                '''
                if(subFrameCount == 0):
                    frame_diff = cv2.absdiff(frame, prev_frame)
                    gray_diff = cv2.cvtColor(frame_diff, cv2.COLOR_BGR2GRAY)
                    #Remove the noise and do the threshold
                    # gray_diff = cv2.morphologyEx(gray_diff, cv2.KERNEL_SMOOTH, kernel)
                    # gray_diff = cv2.dilate(gray_diff, kernel, iterations=1)
                    # gray_diff = cv2.erode(gray_diff, kernel, iterations=1)
                    # # gray_diff = cv2.morphologyEx(gray_diff, cv2.MORPH_OPEN, kernel)
                    # # gray_diff = cv2.morphologyEx(gray_diff, cv2.MORPH_CLOSE, kernel)
                    # gray_diff = cv2.erode(gray_diff, kernel, iterations=1)
                    # gray_diff = cv2.dilate(gray_diff, kernel, iterations=1)
                    # cv2.cv.Smooth(gray_diff, gray_diff, cv2.cv.CV_BLUR, 5,5)
                    # cv2.cv.MorphologyEx(gray_diff, gray_diff, None, None, cv2.cv.CV_MOP_OPEN)
                    # cv2.cv.MorphologyEx(gray_diff, gray_diff, None, None, cv2.cv.CV_MOP_CLOSE)
                    # cv2.cv.Threshold(gray_diff, gray_diff, 10, 255, cv2.cv.CV_THRESH_BINARY_INV)
                    #
                    # mhi_vis = cv2.dilate(mhi_vis, kernel, iterations=2)
                    # mhi_vis = cv2.erode(mhi_vis, kernel, iterations=2)
                    # mhi_vis = cv2.morphologyEx(mhi_vis, cv2.MORPH_OPEN, kernel)
                    # mhi_vis = cv2.morphologyEx(mhi_vis, cv2.MORPH_CLOSE, kernel)
                    # mhi_vis = cv2.erode(mhi_vis, kernel, iterations=2)
                    # mhi_vis = cv2.dilate(mhi_vis, kernel, iterations=2)
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

                    prev_frame = frame.copy()
                    if 0xFF & cv2.waitKey(5) == 27:
                        break
                    cv2.waitKey(25)
                '''
                #################################################################################SAMHI-7
                '''
                if(subFrameCount7 == 0):
                    frame_diff7 = cv2.absdiff(frame, prev_frame7)
                    gray_diff7 = cv2.cvtColor(frame_diff7, cv2.COLOR_BGR2GRAY)
                    #Remove the noise and do the threshold
                    # gray_diff = cv2.morphologyEx(gray_diff, cv2.KERNEL_SMOOTH, kernel)
                    # gray_diff = cv2.dilate(gray_diff, kernel, iterations=1)
                    # gray_diff = cv2.erode(gray_diff, kernel, iterations=1)
                    # # gray_diff = cv2.morphologyEx(gray_diff, cv2.MORPH_OPEN, kernel)
                    # # gray_diff = cv2.morphologyEx(gray_diff, cv2.MORPH_CLOSE, kernel)
                    # gray_diff = cv2.erode(gray_diff, kernel, iterations=1)
                    # gray_diff = cv2.dilate(gray_diff, kernel, iterations=1)
                    # cv2.cv.Smooth(gray_diff, gray_diff, cv2.cv.CV_BLUR, 5,5)
                    # cv2.cv.MorphologyEx(gray_diff, gray_diff, None, None, cv2.cv.CV_MOP_OPEN)
                    # cv2.cv.MorphologyEx(gray_diff, gray_diff, None, None, cv2.cv.CV_MOP_CLOSE)
                    # cv2.cv.Threshold(gray_diff, gray_diff, 10, 255, cv2.cv.CV_THRESH_BINARY_INV)
                    #
                    # mhi_vis = cv2.dilate(mhi_vis, kernel, iterations=2)
                    # mhi_vis = cv2.erode(mhi_vis, kernel, iterations=2)
                    # mhi_vis = cv2.morphologyEx(mhi_vis, cv2.MORPH_OPEN, kernel)
                    # mhi_vis = cv2.morphologyEx(mhi_vis, cv2.MORPH_CLOSE, kernel)
                    # mhi_vis = cv2.erode(mhi_vis, kernel, iterations=2)
                    # mhi_vis = cv2.dilate(mhi_vis, kernel, iterations=2)
                    thrs = cv2.getTrackbarPos('threshold', 'Output')
                    ret7, motion_mask7 = cv2.threshold(gray_diff7, thrs, 1, cv2.THRESH_BINARY)
                    timestamp7 = clock()
                    cv2.updateMotionHistory(motion_mask7, motion_history7, timestamp7, MHI_DURATION)

                    mg_mask7, mg_orient7 = cv2.calcMotionGradient( motion_history7, MAX_TIME_DELTA, MIN_TIME_DELTA, apertureSize=5 )
                    seg_mask7, seg_bounds7 = cv2.segmentMotion(motion_history7, timestamp7, MAX_TIME_DELTA)

                    visual_name7 = visuals[cv2.getTrackbarPos('visual', 'Output7')]
                    if visual_name7 == 'input':
                        vis7 = frame.copy()
                    elif visual_name == 'frame_diff':
                        vis7 = frame_diff7.copy()
                    elif visual_name == 'motion_hist':
                        vis7 = np.uint8(np.clip((motion_history7-(timestamp7-MHI_DURATION)) / MHI_DURATION, 0, 1)*255)
                        vis7 = cv2.cvtColor(vis7, cv2.COLOR_GRAY2BGR)
                    elif visual_name == 'grad_orient':
                        hsv7[:,:,0] = mg_orient7/2
                        hsv7[:,:,2] = mg_mask7*255
                        vis7 = cv2.cvtColor(hsv7, cv2.COLOR_HSV2BGR)

                    for i, rect in enumerate([(0, 0, w, h)] + list(seg_bounds7)):
                        x, y, rw, rh = rect
                        area = rw*rh
                        if area < 64**2:
                            continue
                        silh_roi7   = motion_mask7   [y:y+rh,x:x+rw]
                        orient_roi7 = mg_orient7     [y:y+rh,x:x+rw]
                        mask_roi7  = mg_mask7       [y:y+rh,x:x+rw]
                        mhi_roi7   = motion_history7[y:y+rh,x:x+rw]
                        if cv2.norm(silh_roi7, cv2.NORM_L1) < area*0.05:
                            continue
                        angle7 = cv2.calcGlobalOrientation(orient_roi7, mask_roi7, mhi_roi7, timestamp7, MHI_DURATION)
                        color7 = ((255, 0, 0), (0, 0, 255))[i == 0]
                        draw_motion_comp(vis7, rect, angle7, color7)

                    visCopy7 = vis7.copy()
                    draw_str(visCopy7, (20, 20), visual_name)
                    cv2.imshow('Output7', visCopy7)

                    prev_frame7 = frame.copy()
                    if 0xFF & cv2.waitKey(5) == 27:
                        break
                    cv2.waitKey(25)

                '''
                #################################################################################SAMHI-14
                '''
                if(subFrameCount14 == 0):
                    frame_diff14 = cv2.absdiff(frame, prev_frame)
                    gray_diff14 = cv2.cvtColor(frame_diff14, cv2.COLOR_BGR2GRAY)
                    #Remove the noise and do the threshold
                    # gray_diff = cv2.morphologyEx(gray_diff, cv2.KERNEL_SMOOTH, kernel)
                    # gray_diff = cv2.dilate(gray_diff, kernel, iterations=1)
                    # gray_diff = cv2.erode(gray_diff, kernel, iterations=1)
                    # # gray_diff = cv2.morphologyEx(gray_diff, cv2.MORPH_OPEN, kernel)
                    # # gray_diff = cv2.morphologyEx(gray_diff, cv2.MORPH_CLOSE, kernel)
                    # gray_diff = cv2.erode(gray_diff, kernel, iterations=1)
                    # gray_diff = cv2.dilate(gray_diff, kernel, iterations=1)
                    # cv2.cv.Smooth(gray_diff, gray_diff, cv2.cv.CV_BLUR, 5,5)
                    # cv2.cv.MorphologyEx(gray_diff, gray_diff, None, None, cv2.cv.CV_MOP_OPEN)
                    # cv2.cv.MorphologyEx(gray_diff, gray_diff, None, None, cv2.cv.CV_MOP_CLOSE)
                    # cv2.cv.Threshold(gray_diff, gray_diff, 10, 255, cv2.cv.CV_THRESH_BINARY_INV)
                    #
                    # mhi_vis = cv2.dilate(mhi_vis, kernel, iterations=2)
                    # mhi_vis = cv2.erode(mhi_vis, kernel, iterations=2)
                    # mhi_vis = cv2.morphologyEx(mhi_vis, cv2.MORPH_OPEN, kernel)
                    # mhi_vis = cv2.morphologyEx(mhi_vis, cv2.MORPH_CLOSE, kernel)
                    # mhi_vis = cv2.erode(mhi_vis, kernel, iterations=2)
                    # mhi_vis = cv2.dilate(mhi_vis, kernel, iterations=2)
                    thrs = cv2.getTrackbarPos('threshold', 'Output14')
                    ret14, motion_mask14 = cv2.threshold(gray_diff14, thrs, 1, cv2.THRESH_BINARY)
                    timestamp14 = clock()
                    cv2.updateMotionHistory(motion_mask14, motion_history14, timestamp14, MHI_DURATION)

                    mg_mask14, mg_orient14 = cv2.calcMotionGradient( motion_history14, MAX_TIME_DELTA, MIN_TIME_DELTA, apertureSize=5 )
                    seg_mask14, seg_bounds14 = cv2.segmentMotion(motion_history14, timestamp14, MAX_TIME_DELTA)

                    visual_name = visuals[cv2.getTrackbarPos('visual', 'Output14')]
                    if visual_name == 'input':
                        vis14 = frame.copy()
                    elif visual_name == 'frame_diff':
                        vis14 = frame_diff14.copy()
                    elif visual_name == 'motion_hist':
                        vis14 = np.uint8(np.clip((motion_history14-(timestamp14-MHI_DURATION)) / MHI_DURATION, 0, 1)*255)
                        vis14 = cv2.cvtColor(vis14, cv2.COLOR_GRAY2BGR)
                    elif visual_name == 'grad_orient':
                        hsv14[:,:,0] = mg_orient14/2
                        hsv14[:,:,2] = mg_mask14*255
                        vis14 = cv2.cvtColor(hsv14, cv2.COLOR_HSV2BGR)

                    for i, rect in enumerate([(0, 0, w, h)] + list(seg_bounds14)):
                        x, y, rw, rh = rect
                        area = rw*rh
                        if area < 64**2:
                            continue
                        silh_roi14   = motion_mask14   [y:y+rh,x:x+rw]
                        orient_roi14 = mg_orient14     [y:y+rh,x:x+rw]
                        mask_roi14   = mg_mask14       [y:y+rh,x:x+rw]
                        mhi_roi14    = motion_history14[y:y+rh,x:x+rw]
                        if cv2.norm(silh_roi14, cv2.NORM_L1) < area*0.05:
                            continue
                        angle14 = cv2.calcGlobalOrientation(orient_roi14, mask_roi14, mhi_roi14, timestamp14, MHI_DURATION)
                        color14 = ((255, 0, 0), (0, 0, 255))[i == 0]
                        draw_motion_comp(vis14, rect, angle14, color14)

                    visCopy14 = vis14.copy()
                    draw_str(visCopy14, (20, 20), visual_name)
                    cv2.imshow('Output14', visCopy14)

                    prev_frame14 = frame.copy()
                    if 0xFF & cv2.waitKey(5) == 27:
                        break
                    cv2.waitKey(25)

                subFrameCount = subFrameCount + 1
                subFrameCount7 = subFrameCount7 + 1
                subFrameCount14 = subFrameCount14 + 1

                if(subFrameCount > skipFrames):
                    subFrameCount = 0

                if(subFrameCount7 > skipFrames7):
                    subFrameCount7 = 0

                if(subFrameCount14 > skipFrames14):
                    subFrameCount14 = 0

                frameCounter = frameCounter + 1
            with open("mhiInfo", 'a+') as mhiFile:
                    mhiFile.write("\n======================================================================================================\n")
                    for row in motion_history:
                        # print(row)
                        mhiFile.write(' '.join(str(x) for x in row)+"\n")

            if(visual_name == 'motion_hist'):
                mhi_vis = np.uint8(np.clip((motion_history-(timestamp-MHI_DURATION)) / MHI_DURATION, 0, 1)*255)
                mhi_vis = cv2.cvtColor(mhi_vis, cv2.COLOR_GRAY2BGR)

                mhi_vis7 = np.uint8(np.clip((motion_history7-(timestamp7-MHI_DURATION)) / MHI_DURATION, 0, 1)*255)
                mhi_vis7 = cv2.cvtColor(mhi_vis7, cv2.COLOR_GRAY2BGR)

                mhi_vis14 = np.uint8(np.clip((motion_history14-(timestamp14-MHI_DURATION)) / MHI_DURATION, 0, 1)*255)
                mhi_vis14 = cv2.cvtColor(mhi_vis14, cv2.COLOR_GRAY2BGR)
            else:
                hsv[:,:,0] = mg_orient/2
                hsv[:,:,2] = mg_mask*255
                mhi_vis = cv2.cvtColor(hsv, cv2.COLOR_HSV2BGR)

                hsv7[:,:,0] = mg_orient7/2
                hsv7[:,:,2] = mg_mask7*255
                mhi_vis7 = cv2.cvtColor(hsv7, cv2.COLOR_HSV2BGR)

                hsv14[:,:,0] = mg_orient14/2
                hsv14[:,:,2] = mg_mask14*255
                mhi_vis14 = cv2.cvtColor(hsv14, cv2.COLOR_HSV2BGR)


            #Remove the noise and do the threshold
            # cv2.cv.Smooth(mhi_vis, mhi_vis, cv2.cv.CV_BLUR, 5,5)
            # cv2.cv.MorphologyEx(mhi_vis, mhi_vis, None, None, cv2.cv.CV_MOP_OPEN)
            # cv2.cv.MorphologyEx(mhi_vis, mhi_vis, None, None, cv2.cv.CV_MOP_CLOSE)
            # cv2.cv.Threshold(mhi_vis, mhi_vis, 10, 255, cv2.cv.CV_THRESH_BINARY_INV)
            # #
            # mhi_vis = cv2.(mhi_vis, kernel, iterations=2)
            # mhi_vis = cv2.erode(mhi_vis, kernel, iterations=2)
            # mhi_vis = cv2.morphologyEx(mhi_vis, cv2.MORPH_OPEN, kernel)
            # mhi_vis = cv2.morphologyEx(mhi_vis, cv2.MORPH_CLOSE, kernel)
            # mhi_vis = cv2.erode(mhi_vis, kernel, iterations=2)
            # mhi_vis = cv2.dilate(mhi_vis, kernel, iterations=2)

            ##Start extracting features
            sift = cv2.SIFT()
            denseDetector = cv2.FeatureDetector_create(sDetector) ##using Dense Feature Detector

            kp = detector.detect(mhi_vis)

            kp7 = detector.detect(mhi_vis7)

            kp14 = detector.detect(mhi_vis14)

            # kp2, des2 = sift.compute(mhi_vis,kp)
            # img=cv2.drawKeypoints(mhi_vis,kp2)

            print('KeyPoints Length:: ',len(kp))

            hasAtleastOneKP = False

            ##Check if there are any detected keypoints before processing.
            if len(kp) > 0:
                hasAtleastOneKP = True
                features = extractor.compute(mhi_vis,kp)
                featuresDes = features[1]
                # print('Descriptors:: ',featuresDes)
                print('Descriptors Length:: ',len(featuresDes))
                print('Descriptors Shape:: ',featuresDes.shape)

                if(featureDesSAHMIS is None):
                    featureDesSAHMIS = featuresDes
                else:
                    featureDesSAHMIS = np.append(featureDesSAHMIS,featuresDes,axis=0)

                if(oper == "extractTrainingVocabulary"):
                    bowTrainer.add(featureDesSAHMIS)
                    saveTrainFeaturesDes.append(featureDesSAHMIS)
                    cv2.imwrite('mhiImages\\'+file.split("\\")[2]+'_mhi.jpg',mhi_vis)
                elif(oper=="extractBOWDescriptor"):
                    bowDescriptor = bowDE.compute(mhi_vis, kp)
                    # descriptors.push_back(bowDescriptor);
                    # print('bowDescriptor:: ',bowDescriptor)
                    print('bowDescriptor Length:: ',len(bowDescriptor))
                    print('bowDescriptor Shape:: ',bowDescriptor.shape)
                    if(fileType=="train"):
                        img=cv2.drawKeypoints(mhi_vis,kp)
                        cv2.imwrite('keyPoints\\'+file.split("\\")[2]+'_keypoints.jpg',img)
                    if(bowDescriptorSAHMIS is None):
                        bowDescriptorSAHMIS = bowDescriptor
                    else:
                        # bowDescriptorSAHMIS = np.append(bowDescriptorSAHMIS,bowDescriptor)
                        bowDescriptorSAHMIS = np.sum([bowDescriptorSAHMIS,bowDescriptor],axis=0)
            else:
                featuresDes = np.zeros((1,featureSize),np.float32)
                if(featureDesSAHMIS is None):
                    featureDesSAHMIS = featuresDes
                else:
                    featureDesSAHMIS = np.append(featureDesSAHMIS,featuresDes,axis=0)
                bowDescriptor = np.zeros((1,1000),np.float32)
                if(bowDescriptorSAHMIS is None):
                    bowDescriptorSAHMIS = bowDescriptor
                else:
                    # bowDescriptorSAHMIS = np.append(bowDescriptorSAHMIS,bowDescriptor)
                    bowDescriptorSAHMIS = np.sum([bowDescriptorSAHMIS,bowDescriptor],axis=0)
                print("No SAMHI-1 Key points were detectected for this image..")





            ##Check if there are any detected keypoints before processing.
            if len(kp7) > 0:
                hasAtleastOneKP = True
                features7 = extractor.compute(mhi_vis7,kp7)
                featuresDes7 = features7[1]
                # print('Descriptors7:: ',featuresDes7)
                print('Descriptors7 Length:: ',len(featuresDes7))
                print('Descriptors7 Shape:: ',featuresDes7.shape)

                if(featureDesSAHMIS is None):
                    featureDesSAHMIS = featuresDes7
                else:
                    featureDesSAHMIS = np.append(featureDesSAHMIS,featuresDes7,axis=0)

                if(oper == "extractTrainingVocabulary"):
                    cv2.imwrite('mhiImages\\'+file.split("\\")[2]+'_mhi7.jpg',mhi_vis7)
                elif(oper=="extractBOWDescriptor"):
                    bowDescriptor7 = bowDE.compute(mhi_vis7, kp7)
                    # descriptors.push_back(bowDescriptor);
                    # print('bowDescriptor7:: ',bowDescriptor7)
                    print('bowDescriptor7 Length:: ',len(bowDescriptor7))
                    print('bowDescriptor7 Shape:: ',bowDescriptor7.shape)
                    if(fileType=="train"):
                        img7=cv2.drawKeypoints(mhi_vis7,kp7)
                        cv2.imwrite('keyPoints\\'+file.split("\\")[2]+'_keypoints_7.jpg',img7)
                    if(bowDescriptorSAHMIS is None):
                        bowDescriptorSAHMIS = bowDescriptor7
                    else:
                        # print("bowDescriptor7=> ",bowDescriptor7)
                        # bowDescriptorSAHMIS = np.append(bowDescriptorSAHMIS,bowDescriptor7)
                        bowDescriptorSAHMIS = np.sum([bowDescriptorSAHMIS,bowDescriptor7],axis=0)
            else:
                featuresDes7 = np.zeros((1,featureSize),np.float32)
                if(featureDesSAHMIS is None):
                    featureDesSAHMIS = featuresDes7
                else:
                    featureDesSAHMIS = np.append(featureDesSAHMIS,featuresDes7,axis=0)
                bowDescriptor7 = np.zeros((1,1000),np.float32)
                if(bowDescriptorSAHMIS is None):
                    bowDescriptorSAHMIS = bowDescriptor7
                else:
                    # bowDescriptorSAHMIS = np.append(bowDescriptorSAHMIS,bowDescriptor7)
                    bowDescriptorSAHMIS = np.sum([bowDescriptorSAHMIS,bowDescriptor7],axis=0)
                print("No SAMHI-7 Key points were detectected for this image..")

            ##Check if there are any detected keypoints before processing.
            if len(kp14) > 0:
                hasAtleastOneKP = True
                features14 = extractor.compute(mhi_vis14,kp14)
                featuresDes14 = features14[1]
                # print('Descriptor14:: ',featuresDes14)
                print('Descriptors14 Length:: ',len(featuresDes14))
                print('Descriptors14 Shape:: ',featuresDes14.shape)

                if(featureDesSAHMIS is None):
                    featureDesSAHMIS = featuresDes14
                else:
                    featureDesSAHMIS = np.append(featureDesSAHMIS,featuresDes14,axis=0)

                if(oper == "extractTrainingVocabulary"):
                    cv2.imwrite('mhiImages\\'+file.split("\\")[2]+'_mhi14.jpg',mhi_vis14)
                elif(oper=="extractBOWDescriptor"):
                    bowDescriptor14 = bowDE.compute(mhi_vis14, kp14)
                    # descriptors.push_back(bowDescriptor);
                    # print('bowDescriptor14:: ',bowDescriptor14)
                    print('bowDescriptor14 Length:: ',len(bowDescriptor14))
                    print('bowDescriptor14 Shape:: ',bowDescriptor7.shape)
                    if(fileType=="train"):
                        img14=cv2.drawKeypoints(mhi_vis14,kp14)
                        cv2.imwrite('keyPoints\\'+file.split("\\")[2]+'_keypoints_14.jpg',img14)

                    if(bowDescriptorSAHMIS is None):
                        bowDescriptorSAHMIS = bowDescriptor14
                    else:
                        # bowDescriptorSAHMIS = np.append(bowDescriptorSAHMIS,bowDescriptor14)
                        bowDescriptorSAHMIS = np.sum([bowDescriptorSAHMIS,bowDescriptor14],axis=0)
            else:
                featuresDes14 = np.zeros((1,featureSize),np.float32)
                if(featureDesSAHMIS is None):
                    featureDesSAHMIS = featuresDes14
                else:
                    featureDesSAHMIS = np.append(featureDesSAHMIS,featuresDes14,axis=0)
                bowDescriptor14 = np.zeros((1,1000),np.float32)
                if(bowDescriptorSAHMIS is None):
                    bowDescriptorSAHMIS = bowDescriptor14
                else:
                    # bowDescriptorSAHMIS = np.append(bowDescriptorSAHMIS,bowDescriptor14)
                    bowDescriptorSAHMIS = np.sum([bowDescriptorSAHMIS,bowDescriptor14],axis=0)
                print("No SAMHI-14 Key points were detectected for this image..")

            if(hasAtleastOneKP):
                if(oper == "extractTrainingVocabulary"):
                    # print('featureDesSAHMIS:: ',featureDesSAHMIS)
                    print('featureDesSAHMIS Length:: ',len(featureDesSAHMIS))
                    print('featureDesSAHMIS Shape:: ',featureDesSAHMIS.shape)
                    bowTrainer.add(featureDesSAHMIS)
                    saveTrainFeaturesDes.append(featureDesSAHMIS)
                else:
                    print('bowDescriptorSAHMIS:: ',bowDescriptorSAHMIS)
                    print('bowDescriptorSAHMIS Length:: ',len(bowDescriptorSAHMIS))
                    print('bowDescriptorSAHMIS Shape:: ',bowDescriptorSAHMIS.shape)
                    ##Check if the operation on training data or test data
                    if(fileType=="train"):
                        trainData.append(bowDescriptorSAHMIS)
                        trainLabels.append(label)
                        # trainLabels.append(label)
                        # trainLabels.append(label)
                    else:
                        testData.append(bowDescriptorSAHMIS)
                        testLabels.append(label)
                        # testLabels.append(label)
                        # testLabels.append(label)

            print(file.split("\\")[2])

            # cv2.imwrite('keyPoints\\'+file.split("\\")[2]+'_keypoints.jpg',img)
            cv2.waitKey(25)

    cv2.destroyAllWindows()
'''
##Main Function
'''
if __name__ == '__main__':
    global trainData,trainLabels,testLabels,testData,classifier,saveTrainFeaturesDes

    print("What do you want to do?\n1. Train\n 2. Test")
    print 'enter "q" at any stage to quit.'
    # sel = raw_input('Type 1 to Train data or 2 to Test: ')
    createTrainTestFiles('dataset-1') #Split the dataset
    readTrainTestFileList(pTestFile="Data/testdemonewline.txt")

    ##DEBUG BLOCK
    myList = [[1, 2, 3],[1, 2, 3]]
    myList1 = np.array([[1, 2, 3],[1, 2, 3]])
    myList2 = np.array([[11, 22, 33],[11, 22, 23]])
    print(np.append(myList1,myList2))
    print(np.append(myList1,myList2,axis=1))

    # exit(0)
    np.array(myList).dump(open('conf/tempTest_v'+version+'.dat','wb'))

    myList2 = np.load(open('conf/tempTest_v'+version+'.dat','rb'))
    print(myList)

    for ele in np.array(myList2):
        print(ele)

    if not(onlyTest):
        if(not readTrainFeaturesFromFile):
            with open(trainFeatureFileName, 'w+') as trainFTFile:
                pass
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

        print("TrainingData : ", trainData)
        print("TrainingLabel : ", trainLabels)
        print("TrainingData Shape: ",  np.float32(trainData).shape)
        print("TrainingLabel Shape: ", np.float32(trainLabels).shape)

        trainData = np.float32(trainData).reshape(-1,dictSize)
        trainLabels = np.float32(trainLabels).reshape(-1,1)

        print("TrainingData Shape: ", trainData.shape)
        print("TrainingLabel Shape: ", trainLabels.shape)
        print("TrainingData : ", trainData)
        print("TrainingLabel : ", trainLabels)

        print("Training classifier..")
        classifier.train(trainData, trainLabels, params=svm_params);
        classifier.save('conf/svm_data_v'+version+'.dat')
        # bowTrainer.save('bowTrainer.dat')
        # bowDE.save('bowDE.dat')
        # descriptors.save('bowDescriptor.dat')
        # dictionary.save('bowDictionary.dat')
    else:
        saveTrainFeaturesDes1 = np.load(open('conf/trainFeatures_v'+version+'.dat', 'rb'))

        for savedFeatures in saveTrainFeaturesDes1:
            bowTrainer.add(savedFeatures)
        ##Get BOW descriptors
        descriptors = bowTrainer.getDescriptors();
        print("Clustering ",len(descriptors)," Features..")
        dictionary = bowTrainer.cluster();
        bowDE.setVocabulary(dictionary);
        # bowTrainer.load('bowTrainer.dat')
        # bowDE.load('bowDE.dat')
        # descriptors.load('bowDescriptor.dat')
        # dictionary.load('bowDictionary.dat')
        classifier.load('conf/svm_data_v'+version+'.dat')

    ######     Now testing      ########################
    testData = []
    testLabels = []

    processTrainingFiles(oper="extractBOWDescriptor", fileType="test")

    print("testData Shape: ", np.float32(testData))
    print("testLabels Shape: ", len(testLabels))
    print("testData : ", testData)
    print("testLabels : ", testLabels)

    testData = np.float32(testData).reshape(-1,dictSize)
    testLabels = np.float32(testLabels).reshape(-1,1)
    result = classifier.predict_all(testData)

    #######   Check Accuracy   ########################
    printPageBreak()
    printPageBreak()

    print("TestLabels: ", testLabels)
    print("Results: ", result);
    mask = result==testLabels
    print("Result Mask: ", mask);

    correct = np.count_nonzero(mask)
    print("Correct Results: ",correct);
    print correct*100.0/result.size





