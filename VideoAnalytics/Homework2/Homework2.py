__author__ = 'Arunkumar'
import numpy as np
import cv2
import sys
import datetime
import random
from colorsys import hsv_to_rgb

options = {} #used to store and display the program options/features
options[0] =  "Object Count + contour detection (without morphological operations)"
options[1] = "Object Count with Erosion + contour detection"
options[2] = "Object Count with dilation + contour detection"
options[3] = "Object Count with Opening + contour detection"
#options[4] = "Color Detection"
options[4] = "Object Count with Closing + contour detection"
options[5] = "Object Count with Gradient + contour detection"
options[6] = "Object Count with TopHat + contour detection"
options[7] = "Object Count with BlackHat + contour detection"

selection = 0
morphOps = ['none','erode','dilate','opening','closing','gradient','tophat','blackhat']

#Kernel for image manipulation
kernel = np.ones((5,5),np.uint8)
# rng = random.random(1234)
random.seed(1234)
# cv2.RNG_UNIFORM(1234)

def nothing(x):
    pass


#Function for counting objects using Morphological filters + contour detection
def objectCountWithErosion(isStream, filterType):
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
    # Starting with 100's to prevent error while masking
    hLower,sLower,vLower = 138,121,110 #99,101,152
    hUpper,sUpper,vUpper = 179,255,255
    iFilter = int(filterType)

    # Creating track bar
    cv2.createTrackbar('hLower', 'Setting Window',0,179,nothing)
    cv2.createTrackbar('sLower', 'Setting Window',0,255,nothing)
    cv2.createTrackbar('vLower', 'Setting Window',0,255,nothing)
    cv2.createTrackbar('hUpper', 'Setting Window',0,180,nothing)
    cv2.createTrackbar('sUpper', 'Setting Window',0,255,nothing)
    cv2.createTrackbar('vUpper', 'Setting Window',0,255,nothing)
    cv2.setTrackbarPos('hLower', 'Setting Window',hLower)
    cv2.setTrackbarPos('sLower', 'Setting Window',sLower)
    cv2.setTrackbarPos('vLower', 'Setting Window',vLower)
    cv2.setTrackbarPos('hUpper', 'Setting Window',hUpper)
    cv2.setTrackbarPos('sUpper', 'Setting Window',sUpper)
    cv2.setTrackbarPos('vUpper', 'Setting Window',vUpper)
    cv2.namedWindow('Original feed with contours | HSV Masked | Morphed Image',cv2.WINDOW_NORMAL)
    while(1):
        startTime = datetime.datetime.now()
        ret, frame = cap.read()
        if(frame == None or not(ret == True)):
            break

        orgImage = frame
        #Color of the ball is red so..
        myColor = np.uint8([[[0,0,255]]])

        og = orgImage

        #HSV conversion
        hsv = cv2.cvtColor(orgImage, cv2.COLOR_BGR2HSV)
        hsvColor = cv2.cvtColor(myColor,cv2.COLOR_BGR2HSV)

        # print 'hsvColor[0][0][0] = ', hsvColor[0][0][0], hsvColor
        # define range of red color in HSV
        # lower_red = np.array([hsvColor[0][0][0]-10,80,0]) #[50,50,110])
        # upper_red = np.array([hsvColor[0][0][0]+10,255,255]) #[255,255,150])


         # get info from track bar and appy to result
        hLower = cv2.getTrackbarPos('hLower','Setting Window')
        sLower = cv2.getTrackbarPos('sLower','Setting Window')
        vLower = cv2.getTrackbarPos('vLower','Setting Window')
        hUpper = cv2.getTrackbarPos('hUpper','Setting Window')
        sUpper = cv2.getTrackbarPos('sUpper','Setting Window')
        vUpper = cv2.getTrackbarPos('vUpper','Setting Window')
        lower_red = np.array([hLower,sLower,vLower]) #[50,50,110]) #lower_red = np.array([160,80,0]) #[50,50,110]) 101.72.152
        upper_red = np.array([hUpper,sUpper,vUpper]) #np.array([180,255,255]) #np.array([180,255,255]) #[255,255,150])
        # lower_red = np.array([160,80,0]) #[50,50,110])
        # upper_red = np.array([180,255,255]) #[255,255,150])
        ##H-10, H+10 range not working for red as hue is zero for red
        # lower_red = np.array([100,100,hsvColor[0][0][0]-10]) #[50,50,110])
        # upper_red = np.array([255,255,hsvColor[0][0][0]+10]) #[255,255,150])

        # Threshold the HSV image to get only red colors
        mask = cv2.inRange(hsv, lower_red, upper_red)

        # Bitwise-AND mask and original image
        res = cv2.bitwise_and(hsv,hsv, mask= mask)

        morphedImg = None
        if filterType == '1':
            morphedImg = cv2.erode(res, kernel, iterations=1)
        elif filterType == '2':
            morphedImg = cv2.dilate(res, kernel, iterations=1)
        elif filterType == '3':
            morphedImg = cv2.morphologyEx(res, cv2.MORPH_OPEN, kernel)
            morphedImg = cv2.morphologyEx(morphedImg, cv2.MORPH_CLOSE, kernel)
            morphedImg = cv2.dilate(morphedImg, kernel, iterations=2)
        elif filterType == '4':
            morphedImg = cv2.morphologyEx(res, cv2.MORPH_CLOSE, kernel)
            morphedImg = cv2.morphologyEx(morphedImg, cv2.MORPH_OPEN, kernel)
        elif filterType == '5':
            morphedImg = cv2.morphologyEx(res, cv2.MORPH_GRADIENT, kernel)
        elif filterType == '6':
            morphedImg = cv2.morphologyEx(res, cv2.MORPH_TOPHAT, kernel)
        elif filterType == '7':
            morphedImg = cv2.morphologyEx(res, cv2.MORPH_BLACKHAT, kernel)
        else:
            morphedImg = res

        imgray1 = cv2.cvtColor(morphedImg,cv2.COLOR_HSV2BGR)
        imgray = cv2.cvtColor(imgray1,cv2.COLOR_BGR2GRAY)
        # gausBlur = cv2.GaussianBlur(imgray,(5,5),0)
        # cannyEdge = cv2.Canny(imgray,35,35*3,apertureSize = 3)
        ret,thresh = cv2.threshold(imgray,127,255,cv2.THRESH_OTSU)
        contours, hierarchy = cv2.findContours(thresh,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE)


        if(len(contours) > 0):
            for ct in range(0,len(contours)):
                x,y,w,ht = cv2.boundingRect(contours[ct])

                scalarColor = [random.uniform(0,255),random.uniform(0,255),random.uniform(0,255)]
                h = random.uniform(0, 179) # Select random green'ish hue from hue wheel
                s = random.uniform(0, 255)
                v = random.uniform(0, 255)
                r, g, b = hsv_to_rgb(h, s, v)
                cv2.rectangle(og,(x-5,y-5),(x+w+5,y+ht+5),(200,200,105),2)
                imgWithCountours = cv2.drawContours(orgImage, contours, -1, (b,g,r), 3)

        cv2.imshow('Original feed with contours | HSV Masked | Morphed Image',
                   np.hstack([orgImage, res, morphedImg]),
        )
        # cv2.imshow('Final image',orgImage)
        print 'Total number of Colored Objects are: ',len(contours)
        #cv2.waitKey(0)
        endTime = datetime.datetime.now()
        diffTime = endTime - startTime
        frameCount = frameCount + 1
        # print 'Time take to process Frame-',frameCount, ' is ', diffTime
        if(frameCount <= 5):
            avgProcessingTimes.append(diffTime)

        # cv2.imshow('Capturing Frames', frame)
        if cv2.waitKey(1) & 0xFF == ord('0'):
            cv2.imwrite('OrgSImage_'+morphOps[iFilter]+'.png', orgImage)
            cv2.imwrite('hsvMaskedSImg_'+morphOps[iFilter]+'.png', res)
            cv2.imwrite('morphedSImg_'+morphOps[iFilter]+'.png', morphedImg)
            break
        if not isStream:
            cv2.imwrite('OrgImage_'+morphOps[iFilter]+'.png', orgImage)
            cv2.imwrite('hsvMaskedImg_'+morphOps[iFilter]+'.png', res)
            cv2.imwrite('morphedImg_'+morphOps[iFilter]+'.png', morphedImg)
            break
    sumTime = None
    if len(avgProcessingTimes) > 0:
        for t in avgProcessingTimes:
            if sumTime == None:
                sumTime = t
            else:
                sumTime = sumTime + t

        avgTime = sumTime/len(avgProcessingTimes)
        # print(sumTime,'::',len(avgProcessingTimes))
        # print 'Average processing time for ',len(avgProcessingTimes),' frames is ', avgTime, ' (hh:mm:ss:ms).'
    #writing the center frame
    # cv2.imwrite(outFileName,frame)
    cv2.waitKey(0)
    cap.release()
    cv2.destroyAllWindows()

#Function for counting objects using Dilation
#Function for counting objects using Opening
#Function for counting objects using Closing


#this is my main function
def main(args):
    print 'main: OpenCV version -> ', cv2.__version__
    options.keys().sort()
    isStream = False
    while(1):
        print "Hello There!!"
        print 'enter "q" at any stage to quit.'
        iStream = raw_input('Do you want to process a real time video? (Y/N): ')
        isStream = False
        if(iStream.lower() in ['y', 'yes']):
            isStream = True
        if iStream.lower() == 'q':
            break

        print 'Please select your options from:'

        for key in options.keys():
            print key,' : ',options[key]
        print 'enter "q" to quit.'
        selection = raw_input("Enter your selection: ")

        print selection
        # Creating a window for later use
        cv2.namedWindow('Setting Window',1)
        if selection in ['0','1','2','3','4','5','6','7']:
            objectCountWithErosion(isStream, selection)
        elif selection.lower() == 'q':
            break
        else:
            print 'Invalid selection! Please try again..'
        print '\n'


######################################################################################################################
if __name__ == "__main__":
   main(sys.argv[1:])
