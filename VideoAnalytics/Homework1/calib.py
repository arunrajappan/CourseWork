__author__ = 'Arunkumar'
import numpy as np
import cv2
import glob
import time

# termination criteria
criteria = (cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 30, 0.001)

# prepare object points, like (0,0,0), (1,0,0), (2,0,0) ....,(6,5,0)
pattern_size = (3, 3)
objp = np.zeros( (np.prod(pattern_size), 3), np.float32 )
objp[:,:2] = np.indices(pattern_size).T.reshape(-1, 2)
# pattern_points *= square_size
# objp = np.zeros((5*7,3), np.float32)
# objp[:,:2] = np.mgrid[0:w,0:h].T.reshape(-1,2)

# Arrays to store object points and image points from all the images.
objpoints = [] # 3d point in real world space
imgpoints = [] # 2d points in image plane.

images = glob.glob('img/*.jpg')

print (len(images))
for fname in images:
    # fname.
    print(fname)
    print(np.indices(pattern_size))
    img = cv2.imread(fname)
    gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
    # cv2.imshow("Current Image",gray)
    # cv2.waitKey(0)
    # time.sleep(10)
    # Find the chess board corners
    ret, corners = cv2.findChessboardCorners(gray, pattern_size, cv2.cv.CV_CALIB_CB_ADAPTIVE_THRESH | cv2.cv.CV_CALIB_CB_FILTER_QUADS | cv2.cv.CV_CALIB_CB_NORMALIZE_IMAGE)
    # time.sleep(200)
    print(ret,corners)
    # If found, add object points, image points (after refining them)
    if ret == True:

        print(pattern_size[0])
        corners2 = cv2.cornerSubPix(gray,corners,(pattern_size[0]*2 + 1, pattern_size[1]*2 + 1), (-1, -1),criteria)


        # Draw and display the corners
        img2 = cv2.drawChessboardCorners(img, pattern_size, corners,ret)
        print(ret,corners2)

        screen_res = 1280, 720
        scale_width = screen_res[0] / img.shape[1]
        scale_height = screen_res[1] / img.shape[0]
        scale = min(scale_width, scale_height)
        window_width = int(img.shape[1] * scale)
        window_height = int(img.shape[0] * scale)
        cv2.namedWindow('dst_rt', cv2.WINDOW_NORMAL)
        cv2.resizeWindow('dst_rt', window_width, window_height)
        cv2.imshow('dst_rt', img)
        cv2.waitKey(0)
        objpoints.append(objp)
        imgpoints.append(corners.reshape(-1, 2))

cv2.destroyAllWindows()