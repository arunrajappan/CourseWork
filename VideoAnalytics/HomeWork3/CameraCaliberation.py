__author__ = 'Arunkumar'
import numpy as np
import cv2
import sys
import datetime
import time, glob
import os
import math


selection = 0
# termination criteria
criteria = (cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 30, 0.001)
square_size = 1.0

# prepare object points, like (0,0,0), (1,0,0), (2,0,0) ....,(6,5,0)
pattern_size = (6, 4)
pattern_points = np.zeros( (np.prod(pattern_size), 3), np.float32 )
pattern_points[:,:2] = np.indices(pattern_size).T.reshape(-1, 2)
pattern_points *= square_size
print(pattern_points)
# Arrays to store object points and image points from all the images.
objpoints = [] # 3d point in real world space
imgpoints = [] # 2d points in image plane.

images = glob.glob('new_images1/*.jpg')
refimages = glob.glob('reference/*.jpg')
imgHeight = 0
imgWeight = 0

#Function to take samples from a real time video
def collectSamples():
    cap = cv2.VideoCapture(0)
    counter = 0
    if not cap.isOpened():
        print 'could not open the video stream '
        return

    cap.set(cv2.cv.CV_CAP_PROP_FRAME_WIDTH,640);
    cap.set(cv2.cv.CV_CAP_PROP_FRAME_HEIGHT,480);
    ready = "1"
    while counter <15:
        ret, frame = cap.read()
        orgImage = frame
        gray = cv2.cvtColor(orgImage,cv2.COLOR_BGR2GRAY)

        cv2.imshow("Current Image",gray)
        # cv2.waitKey(0)
        # ready = raw_input("Press 0 when ready:")
        if(ready == '1'):
            # Find the chess board corners
            ret, corners = cv2.findChessboardCorners(gray, pattern_size, None)# cv2.cv.CV_CALIB_CB_ADAPTIVE_THRESH | cv2.cv.CV_CALIB_CB_FILTER_QUADS | cv2.cv.CV_CALIB_CB_NORMALIZE_IMAGE)

            # If found, add object points, image points (after refining them)
            if ret == True:
                counter = counter + 1
                cv2.imwrite('new_images/checkers-'+str(counter)+'.jpg',gray)
                corners2 = cv2.cornerSubPix(gray,corners,(pattern_size[0]*2 + 1, pattern_size[1]*2 + 1), (-1, -1),criteria)

                # Draw and display the corners
                img2 = cv2.drawChessboardCorners(orgImage, pattern_size, corners,ret)
                print("Height = ",orgImage.shape[1],"Width = ",orgImage.shape[0])
                screen_res = 1280, 720
                scale_width = screen_res[0] / orgImage.shape[1]
                scale_height = screen_res[1] / orgImage.shape[0]
                scale = min(scale_width, scale_height)
                window_width = int(orgImage.shape[1] * scale)
                window_height = int(orgImage.shape[0] * scale)
                cv2.namedWindow('dst_rt', cv2.WINDOW_NORMAL)
                cv2.resizeWindow('dst_rt', window_width, window_height)
                cv2.imshow('dst_rt2', orgImage)
                cv2.waitKey(0)
                objpoints.append(pattern_points)
                imgpoints.append(corners.reshape(-1, 2))
                # time.sleep(5)

        if cv2.waitKey(1) & 0xFF == ord('0'):
            break

    cv2.destroyWindow("dst_rt")
    cv2.destroyAllWindows()


#Function to caliberate the camera
def cameraCaliberation():
    counter = 0
    if not os.path.exists('new_images'):
        os.makedirs('new_images')

    for fname in images:
        orgImage =cv2.imread(fname)
        gray = cv2.cvtColor(orgImage,cv2.COLOR_BGR2GRAY)

        cv2.imshow("Current Image",gray)

        # Find the chess board corners
        ret, corners = cv2.findChessboardCorners(gray, pattern_size, None)# cv2.cv.CV_CALIB_CB_ADAPTIVE_THRESH | cv2.cv.CV_CALIB_CB_FILTER_QUADS | cv2.cv.CV_CALIB_CB_NORMALIZE_IMAGE)

        # If found, add object points, image points (after refining them)
        if ret == True:
            counter = counter + 1
            originCorner = corners[0].ravel()
            # for ik in range(0,len(corners)):
            #     corners[ik][0][0] = corners[ik][0][0] - originCorner[0]
            #     corners[ik][0][1] = corners[ik][0][1] - originCorner[1]
            # print(gray.shape[::-1])

                # print(originCorner[1])

            # corners2 = cv2.cornerSubPix(gray,corners,(pattern_size[0]*2 + 1, pattern_size[1]*2 + 1), (-1, -1),criteria)
            # print((pattern_size[0], pattern_size[1]))
            # cv2.cornerSubPix(gray,corners,(11, 11), (-1, -1),criteria)
            cv2.cornerSubPix(orgImage,corners,(pattern_size[0]*2 + 1, pattern_size[1]*2 + 1), (-1, -1),criteria)

            # Draw and display the corners
            img2 = cv2.drawChessboardCorners(orgImage, pattern_size, corners,ret)
            print("Height = ",orgImage.shape[1],"Width = ",orgImage.shape[0])
            screen_res = 1280, 720
            cv2.rectangle(orgImage,tuple(corners[0].ravel()),tuple(corners[1].ravel()),(255,0,0))
            imgHeight, imgWeight = orgImage.shape[:2]
            scale_width = screen_res[0] / orgImage.shape[1]
            scale_height = screen_res[1] / orgImage.shape[0]
            scale = min(scale_width, scale_height)
            window_width = int(orgImage.shape[1] * scale)
            window_height = int(orgImage.shape[0] * scale)
            cv2.namedWindow('dst_rt', cv2.WINDOW_NORMAL)
            cv2.resizeWindow('dst_rt', window_width, window_height)
            cv2.imshow('dst_rt', orgImage)
            # cv2.waitKey(0)
            objpoints.append(pattern_points)
            imgpoints.append(corners.reshape(-1, 2))
            time.sleep(2)

        if cv2.waitKey(1) & 0xFF == ord('0'):
            break

    cv2.destroyAllWindows()
    rms, camera_matrix, dist_coefs, rvecs, tvecs = cv2.calibrateCamera(objpoints, imgpoints, (imgWeight, imgHeight), None, None)
    print "RMS:", rms
    print "camera matrix:\n", camera_matrix
    print "distortion coefficients: ", dist_coefs.ravel()

    np.save('camera_matrix.dat',camera_matrix)
    np.save("dist_coef.dat",dist_coefs)
    np.save("rvecs.dat",rvecs)
    np.save("tvecs.dat",tvecs)
    np.save("rms.dat",rms)

#Function to compute the axis
def computeCoords():
    camera_matrix = np.load('camera_matrix.dat.npy')
    dist_coefs = np.load("dist_coef.dat.npy")
    rvecs = np.load("rvecs.dat.npy")
    tvecs = np.load("tvecs.dat.npy")
    rms = np.load("rms.dat.npy")
    print("Camera Matrix = ",camera_matrix)
    print("Distortion Coefs = ",dist_coefs)
    print("tvecs = ",tvecs)
    print("rvecs = ",rvecs)
    print("rms = ",rms)
    counter = 0
    for fname in refimages:
        orgImage =cv2.imread(fname)
        gray = cv2.cvtColor(orgImage,cv2.COLOR_BGR2GRAY)

        cv2.imshow("Current Image",gray)
        h,  w = gray.shape[:2]
        newcameramtx, roi=cv2.getOptimalNewCameraMatrix(camera_matrix,dist_coefs,(w,h),1,(w,h))
        dst = cv2.undistort(gray, camera_matrix, dist_coefs, None, newcameramtx)
        # crop the image
        x,y,w,h = roi
        print(x,y,w,h)

        # dst = dst[y:y+h, x:x+w]

        # mapx,mapy = cv2.initUndistortRectifyMap(camera_matrix,dist_coefs,None,newcameramtx,(w,h),5)
        # dst = cv2.remap(gray,mapx,mapy,cv2.INTER_LINEAR)
        # # dst = gray
        # # crop the image
        # x,y,w,h = roi
        # print(x,y,w,h)

        # dst = dst[y:y+h, x:x+w]

        cv2.imwrite('calibresult.png',dst)
        cv2.imshow("CaliberatedResult",dst)
        dst = gray
        newcameramtx = camera_matrix
        ret, corners = cv2.findChessboardCorners(dst, pattern_size, None)# cv2.cv.CV_CALIB_CB_ADAPTIVE_THRESH | cv2.cv.CV_CALIB_CB_FILTER_QUADS | cv2.cv.CV_CALIB_CB_NORMALIZE_IMAGE)

        # If found, add object points, image points (after refining them)
        if ret == True:
            counter = counter + 1

            # cv2.cornerSubPix(gray,corners,(11, 11), (-1, -1),criteria)
            cv2.cornerSubPix(dst,corners,(pattern_size[0]*2 + 1, pattern_size[1]*2 + 1), (-1, -1),criteria)
            print(corners)
            # print(corners[pattern_size[0]].ravel())
            # print(corners[0].ravel())
            # print(corners[0].ravel())
            P00 = corners[0].ravel()
            P01 = corners[1].ravel()
            P10 = corners[pattern_size[0]].ravel()
            factor = 0
            f_x = newcameramtx[0][0]
            f_y = newcameramtx[1][1]
            x_0 = newcameramtx[0][2]
            y_0 = newcameramtx[1][2]
            z_s = 522.0
            xs00 = ((P00[0] * z_s) - (z_s*x_0))/f_x
            xs10 = ((P10[0] * z_s) - (z_s*x_0))/f_x
            ys00 = ((P00[1] * z_s) - (z_s*y_0))/f_y
            ys01 = ((P01[1] * z_s) - (z_s*y_0))/f_y
            pixWidth = math.hypot(P10[0] - P00[0],P10[1] - P00[1])
            pixHeight =  math.hypot(P01[0] - P00[0],P01[1] - P00[1])
            print(dst.shape[1],dst.shape[0])
            print(dst.shape[1]/174.625,dst.shape[0]/297.0)
            factor = [dst.shape[1]/174.625,dst.shape[0]/244.475] #[dst.shape[0]/210.0,dst.shape[1]/297.0]
            actualWidth = (pixWidth * z_s)/(f_x * factor[0])
            actualHeight = (pixHeight * z_s)/(f_y * factor[1])
            print("actualWidth = ",actualWidth," actualHeight = ",actualHeight)


            actual00 = [(P00[0] * z_s)/(f_x * factor[0]),(P00[1] * z_s)/(f_y * factor[1])]
            actual01 = [(P01[0] * z_s)/(f_x * factor[0]),(P01[1] * z_s)/(f_y * factor[1])]
            actual10 = [(P10[0] * z_s)/(f_x * factor[0]),(P10[1] * z_s)/(f_y * factor[1])]

            actual00 = [((P00[0]- x_0) * z_s)/(f_x * factor[0]),((P00[1]- y_0) * z_s)/(f_y * factor[1])]
            actual01 = [((P01[0]- x_0) * z_s)/(f_x * factor[0]),((P01[1]- y_0) * z_s)/(f_y * factor[1])]
            actual10 = [((P10[0]- x_0) * z_s)/(f_x * factor[0]),((P10[1]- y_0) * z_s)/(f_y * factor[1])]

            actWidth = math.hypot(actual10[0] - actual00[0],actual10[1] - actual00[1])
            actHeight = math.hypot(actual01[0] - actual00[0],actual01[1] - actual00[1])
            print("actWidth = ",actWidth," actHeight = ",actHeight)
            print(f_x,f_y,x_0,y_0)
            print(xs00,xs10,ys00,ys01)
            print(xs00-xs10,ys00-ys01)


            # Draw and display the corners
            img2 = cv2.drawChessboardCorners(dst, pattern_size, corners,ret)
            print("Height = ",dst.shape[1],"Width = ",dst.shape[0])
            screen_res = 1280, 720
            cv2.rectangle(dst,tuple(corners[0].ravel()),tuple(corners[1].ravel()),(255,0,0))
            imgHeight, imgWeight = dst.shape[:2]
            scale_width = screen_res[0] / dst.shape[1]
            scale_height = screen_res[1] / dst.shape[0]
            scale = min(scale_width, scale_height)
            window_width = int(dst.shape[1] * scale)
            window_height = int(dst.shape[0] * scale)
            cv2.namedWindow('dst_rt', cv2.WINDOW_NORMAL)
            cv2.resizeWindow('dst_rt', window_width, window_height)
            cv2.imshow('dst_rt', dst)
            time.sleep(2)

        if cv2.waitKey(1) & 0xFF == ord('0'):
            break

        # mean_error = 0
        # tot_error = 0
        # for i in xrange(len(objpoints)):
        #     imgpoints2, _ = cv2.projectPoints(objpoints[i], rvecs[i], tvecs[i], camera_matrix, dist_coefs)
        #     error = cv2.norm(imgpoints[i],imgpoints2, cv2.NORM_L2)/len(imgpoints2)
        #     mean_error += error
        #
        # print "total error: ", mean_error/len(objpoints)
        cv2.waitKey(0)
    cv2.destroyAllWindows()


def calcSizeByReference():
    referenceImg = cv2.imread('rImage/checkers75.jpg')
    rGray = cv2.cvtColor(referenceImg,cv2.COLOR_BGR2GRAY)
    tImages = glob.glob('tImage/*.jpg')
    rWDinMM = 34.925
    rHTinMM = 34.925
    rWDinPX = 0.0
    rHTinPX = 0.0
    camera_matrix = np.load('camera_matrix.dat.npy')
    dist_coefs = np.load("dist_coef.dat.npy")
    rvecs = np.load("rvecs.dat.npy")
    tvecs = np.load("tvecs.dat.npy")
    rms = np.load("rms.dat.npy")
    fX = camera_matrix[0][0]
    fY = camera_matrix[1][1]
    zS = 519.75

    ret,thresh = cv2.threshold(rGray,127,255,1)
    contours,h = cv2.findContours(thresh,1,2)

    for cnt in contours:
        approx = cv2.approxPolyDP(cnt,0.01*cv2.arcLength(cnt,True),True)
        if len(approx)==4:
            print "square", cv2.arcLength(cnt,True),approx
            cv2.drawContours(referenceImg,[cnt],0,(0,0,255),-1)
            print(approx[1][0][0],approx[2][0])
            rWDinPX = math.hypot(approx[1][0][0]-approx[0][0][0],approx[1][0][1]-approx[0][0][1])
            rHTinPX = math.hypot(approx[2][0][0]-approx[1][0][0],approx[2][0][1]-approx[1][0][1])
            factorX = 720.0/rGray.shape[1]
            factorY = 720.0/rGray.shape[0]
            print(factorX,factorY,rGray.shape)
            fXzS = fX*factorX/zS
            fYzS = fY*factorY/zS
            zS = 1.2
            print(rWDinPX/fXzS,rHTinPX/fXzS)
            rWDinPX = rWDinPX/fXzS
            rHTinPX=rHTinPX/fXzS

            break

    screen_res = 1280, 720
    imgHeight, imgWeight = referenceImg.shape[:2]
    scale_width = screen_res[0] / referenceImg.shape[1]
    scale_height = screen_res[1] / referenceImg.shape[0]
    scale = min(scale_width, scale_height)
    window_width = int(referenceImg.shape[1] * scale)
    window_height = int(referenceImg.shape[0] * scale)
    cv2.namedWindow('dst_rt', cv2.WINDOW_NORMAL)
    cv2.resizeWindow('dst_rt', window_width, window_height)
    cv2.imshow('dst_rt', referenceImg)
    cv2.waitKey(0)

    for fname in tImages:
        testImage =cv2.imread(fname)
        tGray = cv2.cvtColor(testImage,cv2.COLOR_BGR2GRAY)
        tret,tthresh = cv2.threshold(tGray,127,255,1)
        tcontours,th = cv2.findContours(tthresh,1,2)
        tWDinPX = 0.0
        tHTinPX = 0.0
        cntCount = 0
        ret, corners = cv2.findChessboardCorners(tGray, pattern_size, None)# cv2.cv.CV_CALIB_CB_ADAPTIVE_THRESH | cv2.cv.CV_CALIB_CB_FILTER_QUADS | cv2.cv.CV_CALIB_CB_NORMALIZE_IMAGE)
        if(ret == True):
            P00 = corners[0].ravel()
            P01 = corners[1].ravel()
            P10 = corners[pattern_size[0]].ravel()
            print(P00,P01,P10)
            cv2.rectangle(testImage,tuple(P00),tuple(P01),(255,0,0))
            cv2.rectangle(testImage,tuple(P00),tuple(P10),(0,0,255))
            tWDinPX = abs(P10[0]-P00[0])
            tHTinPX = abs(P01[1]-P00[1])
            print(P10[0],P00[0])
            print(P01[1],P00[1])
            print(tWDinPX,tHTinPX)
            # cv2.circle(testImage,P00,2,(255,0,0))
            # cv2.circle(testImage,P01,2,(0,255,0))
            # cv2.circle(testImage,P10,2,(0,0,255))
            # tWDinPx = corners
            print(camera_matrix,camera_matrix[1][1])
            zS = 519.75*1.2

            factorX = 720.0/tGray.shape[1]
            factorY = 720.0/tGray.shape[0]
            print(factorX,factorY,tGray.shape)
            fXzS = fX*factorX/zS
            fYzS = fY*factorY/zS
            tWDinMM = (tWDinPX * rWDinMM *fXzS)/rWDinPX
            tHTinMM = (tHTinPX * rHTinMM *fYzS)/rHTinPX
            print("Actual Value in MM (WD,HT) - ", tWDinMM, tHTinMM)


            screen_res = 1280, 720
            imgHeight, imgWeight = testImage.shape[:2]
            scale_width = screen_res[0] / testImage.shape[1]
            scale_height = screen_res[1] / testImage.shape[0]
            scale = min(scale_width, scale_height)
            window_width = int(testImage.shape[1] * scale)
            window_height = int(testImage.shape[0] * scale)
            cv2.namedWindow('dst_rt', cv2.WINDOW_NORMAL)
            cv2.resizeWindow('dst_rt', window_width, window_height)
            cv2.imshow('dst_rt', testImage)
            cv2.waitKey(0)

#this is my main function
def main(args):
    print 'main: OpenCV version -> ', cv2.__version__
    # collectSamples()
    # cameraCaliberation()
    # computeCoords()
    calcSizeByReference()

######################################################################################################################
if __name__ == "__main__":
   main(sys.argv[1:])