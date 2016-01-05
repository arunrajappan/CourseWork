__author__ = 'Arunkumar'
import numpy as np
import cv2
import sys
import datetime

options = {} #used to store and display the program options/features
options[1] = "Capture a video in real-time"
options[2] = "Grab a frame in a captured video"
options[3] = "Convert the image from RGB to HSV color space + RED Color detection"
#options[4] = "Color Detection"
options[4] = "Circle Detection"
options[5] = "Color Detection in saved video"
options[6] = "Color Detection in real time"

selection = 0

#Function to capture and save a video
def captureVideo():
    fileName = raw_input('Please enter the name of the file to be saved: ')
    print 'Capturing video'
    cap = cv2.VideoCapture(0)
    out = cv2.VideoWriter(fileName, cv2.cv.CV_FOURCC('M','J','P','G'),25.0,(640,480), True)

    while(cap.isOpened()):
        ret, frame = cap.read()
        if(ret == True):
            out.write(frame)
            cv2.imshow('Capturing Video', frame)
            if cv2.waitKey(1) & 0xFF == ord('0'):
                break
        else:
            break
    #End recording and release camera and close the file
    cap.release()
    out.release()
    cv2.destroyAllWindows()


#Function to grab a frame from the captured video
def grabFrame():
    fileName = raw_input('Please enter the path/name of the saved video file: ')
    outFileName = raw_input('Please enter the name of the image file to be saved: ')
    print 'Grabbing a frame from the video and saving as '
    cap = cv2.VideoCapture(fileName)
    if not cap.isOpened():
        print 'could not open the video file: ', fileName
    frameCount = 0 # for counting the sequence of the frames

    totalFrames = int(cap.get(cv2.cv.CV_CAP_PROP_FRAME_COUNT))
    print "There are total ",totalFrames,' frames in the video ',fileName
    midRange = totalFrames/2
    #Read frames till the frame# = totalFrames/2
    while(frameCount < midRange):
        ret, frame = cap.read()
        if(frame == None):
            break
        frameCount = frameCount + 1
        cv2.imshow('Capturing Frames', frame)
        cv2.waitKey(25)

    #writing the center frame
    cv2.imwrite(outFileName,frame)
    cap.release()
    cv2.destroyAllWindows()

#Function to convert an image from RGB to HSV color space and this also include color detection
def convertRGBtoHSV():
    fileName = raw_input('Please enter the path/name of the saved image/frame file: ')
    print 'Converting image from RGB to HSV color space and detecting RED Color'

    orgImage = cv2.imread(fileName)

    #Color of the ball is red so..
    myColor = np.uint8([[[0,0,255]]])

    #HSV conversion
    hsv = cv2.cvtColor(orgImage, cv2.COLOR_BGR2HSV)
    hsvColor = cv2.cvtColor(myColor,cv2.COLOR_BGR2HSV)

    print 'hsvColor[0][0][0] = ', hsvColor[0][0][0], hsvColor
    # define range of red color in HSV
    # lower_red = np.array([hsvColor[0][0][0]-10,80,0]) #[50,50,110])
    # upper_red = np.array([hsvColor[0][0][0]+10,255,255]) #[255,255,150])
    lower_red = np.array([160,80,0]) #[50,50,110])
    upper_red = np.array([180,255,255]) #[255,255,150])
    # lower_red = np.array([50,50,20]) #[50,50,110])
    # upper_red = np.array([170,170,255]) #[255,255,150])
    ##H-10, H+10 range not working for red as hue is zero for red
    # lower_red = np.array([100,100,hsvColor[0][0][0]-10]) #[50,50,110])
    # upper_red = np.array([255,255,hsvColor[0][0][0]+10]) #[255,255,150])

    # Threshold the HSV image to get only red colors
    mask = cv2.inRange(hsv, lower_red, upper_red)

    # Bitwise-AND mask and original image
    res = cv2.bitwise_and(orgImage,orgImage, mask= mask)

    cv2.imshow('Original Image',orgImage)
    cv2.imshow('HSV Masked Image',mask)
    cv2.imshow('Final image',res)
    cv2.imwrite('ColorMarkedHSV-'+fileName.split('.')[0]+'.jpg',mask)
    cv2.imwrite('ColorMarked-'+fileName.split('.')[0]+'.jpg',res)
    cv2.waitKey(0)
    cv2.destroyAllWindows()


#Function for color detection in an captured frame
def colorDetection():
    fileName = raw_input('Please enter the path/name of the saved video file: ')
    print 'Color Detection'

#Function for circle detection in a given frame
def circleDetection():
    fileName = raw_input('Please enter the path/name of the saved image file: ')
    print 'Detecting circles in the given image/frame'
    orgImage = cv2.imread(fileName)
    output = orgImage.copy()
    # orgImage = cv2.medianBlur(orgImage,5)
    orgImage = cv2.blur(orgImage,(9,9))
    grayImage = cv2.cvtColor(orgImage, cv2.COLOR_BGR2GRAY)

    circles = cv2.HoughCircles(grayImage,cv2.cv.CV_HOUGH_GRADIENT,1,len(grayImage)/8,param1=50,param2=30,minRadius=0,maxRadius=0)
    # detect circles in the image
    #circles = cv2.HoughCircles(grayImage, cv2.cv.CV_HOUGH_GRADIENT, 1.2, 100)
    # ensure at least some circles were found
    # if circles is not None:
    #     # convert the (x, y) coordinates and radius of the circles to integers
    #     circles = np.round(circles[0, :]).astype("int")
    #
    #     # loop over the (x, y) coordinates and radius of the circles
    #     for (x, y, r) in circles:
    #         # draw the circle in the output image, then draw a rectangle
    #         # corresponding to the center of the circle
    #         cv2.circle(output, (x, y), r, (0, 255, 0), 4)
    #         cv2.rectangle(output, (x - 5, y - 5), (x + 5, y + 5), (0, 128, 255), -1)
    #
    #     # show the output image
    #     cv2.imshow("output", np.hstack([orgImage, output]))
    #     cv2.waitKey(0)
    if circles is not None:
        circles = np.uint16(np.around(circles))
        for i in circles[0,:]:
            # draw the outer circle
            cv2.circle(grayImage,(i[0],i[1]),i[2],(0,255,0),2)
            # draw the center of the circle
            cv2.circle(grayImage,(i[0],i[1]),2,(0,0,255),3)

    cv2.imshow('detected circles',grayImage)
    cv2.imwrite('CircleDetection-'+fileName.split('.')[0]+'.jpg',grayImage)
    # cv2.imshow("output", np.hstack([orgImage, grayImage]))
    cv2.waitKey(0)

    cv2.destroyAllWindows()

#Function to perform color detection on the saved video
def offlineColorDetection():
    fileName = raw_input('Please enter the path/name of the saved video file: ')
    print 'Performing real time color detection in the saved video for first 5 frames and taking the average time.'
    cap = cv2.VideoCapture(fileName)
    if not cap.isOpened():
        print 'could not open the video file: ', fileName
        return
    frameCount = 0 # for counting the sequence of the frames

    totalFrames = int(cap.get(cv2.cv.CV_CAP_PROP_FRAME_COUNT))
    print "There are total ",totalFrames,' frames in the video ',fileName
    midRange = totalFrames/2
    #Read frames till the frame# = totalFrames/2

    avgProcessingTimes = []

    while(1):
        startTime = datetime.datetime.now()
        ret, frame = cap.read()
        if(frame == None):
            break

        orgImage = frame
        #Color of the ball is red so..
        myColor = np.uint8([[[0,0,255 ]]])

        #HSV conversion
        hsv = cv2.cvtColor(orgImage, cv2.COLOR_BGR2HSV)
        hsvColor = cv2.cvtColor(myColor,cv2.COLOR_BGR2HSV)

        # print 'hsvColor[0][0][0] = ', hsvColor[0][0][0], hsvColor
        # define range of red color in HSV
        lower_red = np.array([160,80,0]) #[50,50,110])
        upper_red = np.array([180,255,255]) #[255,255,150])
        # lower_red = np.array([-10,80,0]) #[50,50,110])
        # upper_red = np.array([10,255,255]) #[255,255,150])
        ##H-10, H+10 range not working for red as hue is zero for red
        # lower_red = np.array([100,100,hsvColor[0][0][0]-10]) #[50,50,110])
        # upper_red = np.array([255,255,hsvColor[0][0][0]+10]) #[255,255,150])

        # Threshold the HSV image to get only red colors
        mask = cv2.inRange(hsv, lower_red, upper_red)

        # Bitwise-AND mask and original image
        res = cv2.bitwise_and(orgImage,orgImage, mask= mask)

        cv2.imshow('Original Image',orgImage)
        cv2.imshow('HSV Masked Image',mask)
        cv2.imshow('Final image',res)
        cv2.waitKey(0)
        endTime = datetime.datetime.now()
        diffTime = endTime - startTime
        frameCount = frameCount + 1
        print 'Time take to process Frame-',frameCount, ' is ', diffTime
        if(frameCount <=5):
            avgProcessingTimes.append(diffTime)
        # else:
        #     break

        # cv2.imshow('Capturing Frames', frame)
        cv2.waitKey(25)

    sumTime = None
    if len(avgProcessingTimes) > 0:
        for t in avgProcessingTimes:
            if sumTime == None:
                sumTime = t
            else:
                sumTime = sumTime + t

        avgTime = sumTime/len(avgProcessingTimes)
        print(sumTime,'::',len(avgProcessingTimes))
        print 'Average processing time for ',len(avgProcessingTimes),' frames is ', avgTime, ' (hh:mm:ss:ms).'
    #writing the center frame
    # cv2.imwrite(outFileName,frame)
    cv2.waitKey(0)
    cap.release()
    cv2.destroyAllWindows()

#Function to capture and save a video
def realTimeColorDetection():
    # fileName = raw_input('Please enter the path/name of the saved video file: ')
    print 'Performing real time color detection in the video for first 5 frames and taking the average time.'
    cap = cv2.VideoCapture(0)
    if not cap.isOpened():
        print 'could not open the camera '
        return
    frameCount = 0 # for counting the sequence of the frames

    totalFrames = int(cap.get(cv2.cv.CV_CAP_PROP_FRAME_COUNT))
    print "There are total ",totalFrames,' frames in the video '
    midRange = totalFrames/2
    #Read frames till the frame# = totalFrames/2

    avgProcessingTimes = []

    while(1):
        startTime = datetime.datetime.now()
        ret, frame = cap.read()
        if(frame == None or not(ret == True)):
            break

        orgImage = frame
        #Color of the ball is red so..
        myColor = np.uint8([[[0,0,255]]])

        #HSV conversion
        hsv = cv2.cvtColor(orgImage, cv2.COLOR_BGR2HSV)
        hsvColor = cv2.cvtColor(myColor,cv2.COLOR_BGR2HSV)

        # print 'hsvColor[0][0][0] = ', hsvColor[0][0][0], hsvColor
        # define range of red color in HSV
        # lower_red = np.array([hsvColor[0][0][0]-10,80,0]) #[50,50,110])
        # upper_red = np.array([hsvColor[0][0][0]+10,255,255]) #[255,255,150])
        lower_red = np.array([160,80,0]) #[50,50,110])
        upper_red = np.array([180,255,255]) #[255,255,150])
        ##H-10, H+10 range not working for red as hue is zero for red
        # lower_red = np.array([100,100,hsvColor[0][0][0]-10]) #[50,50,110])
        # upper_red = np.array([255,255,hsvColor[0][0][0]+10]) #[255,255,150])

        # Threshold the HSV image to get only red colors
        mask = cv2.inRange(hsv, lower_red, upper_red)

        # Bitwise-AND mask and original image
        res = cv2.bitwise_and(orgImage,orgImage, mask= mask)

        cv2.imshow('Original Image',orgImage)
        cv2.imshow('HSV Masked Image',mask)
        cv2.imshow('Final image',res)
        #cv2.waitKey(0)
        endTime = datetime.datetime.now()
        diffTime = endTime - startTime
        frameCount = frameCount + 1
        print 'Time take to process Frame-',frameCount, ' is ', diffTime
        if(frameCount <= 5):
            avgProcessingTimes.append(diffTime)

        # cv2.imshow('Capturing Frames', frame)
        if cv2.waitKey(1) & 0xFF == ord('0'):
            break

    sumTime = None
    if len(avgProcessingTimes) > 0:
        for t in avgProcessingTimes:
            if sumTime == None:
                sumTime = t
            else:
                sumTime = sumTime + t

        avgTime = sumTime/len(avgProcessingTimes)
        print(sumTime,'::',len(avgProcessingTimes))
        print 'Average processing time for ',len(avgProcessingTimes),' frames is ', avgTime, ' (hh:mm:ss:ms).'
    #writing the center frame
    # cv2.imwrite(outFileName,frame)
    cv2.waitKey(0)
    cap.release()
    cv2.destroyAllWindows()


#this is my main function
def main(args):
    print 'main: OpenCV version -> ', cv2.__version__
    options.keys().sort()

    while(1):
        print 'Please select your options from:'
        for key in options.keys():
            print key,' : ',options[key]
        print 'enter "0" to quit.'
        selection = raw_input("Enter your selection: ")
        print selection
        if selection == '1':
            captureVideo()
        elif selection == '2':
            grabFrame()
        elif selection == '3':
            convertRGBtoHSV()
        elif selection == '9':
            colorDetection()
        elif selection == '4':
            circleDetection()
        elif selection == '5':
            offlineColorDetection()
        elif selection == '6':
            realTimeColorDetection()
        elif selection == '0':
            break
        else:
            print 'Invalid selection! Please try again..'
        print '\n'


######################################################################################################################
if __name__ == "__main__":
   main(sys.argv[1:])
