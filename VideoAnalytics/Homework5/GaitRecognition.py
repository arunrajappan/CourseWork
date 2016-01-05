__author__ = 'Arunkumar'
import numpy as np
import cv2

'''
##Main Function
'''
if __name__ == '__main__':
    gaitClassifier = cv2.CascadeClassifier('conf/haarcascades/haarcascade_fullbody.xml')
    cam = cv2.VideoCapture('data/dataset-1/walk/daria_walk.avi')
    ret, frame = cam.read()
    cv2.imshow('Gait Recognition',frame)
    totalFrames = int(cam.get(cv2.cv.CV_CAP_PROP_FRAME_COUNT))
    frameCounter = 0
    frameCounter = frameCounter +1
    while(frameCounter < totalFrames-1):
        ret, frame = cam.read()
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        gaits = gaitClassifier.detectMultiScale(gray, 1.3, 5)
        for (x,y,w,h) in gaits:
            gray = cv2.rectangle(gray,(x,y),(x+w,y+h),(255,0,0),2)
            roi_gray = gray[y:y+h, x:x+w]
            roi_color = gray[y:y+h, x:x+w]
        cv2.imshow('Gait Recognition',gray)
        cv2.waitKey(25)

