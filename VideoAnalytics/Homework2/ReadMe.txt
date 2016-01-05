This submission has following files:

1. hsvMasked*Img_<ALGORITHM-NAME>.png - Grabbed HSV images of the object
2. Org*Image_<ALGORITHM-NAME>.png - Original image with detected contours drawn on it
3. morphed*Img_<ALGORITHM-NAME>.png - Grabbed images from the output of the execution of the corresponding morphology based algorithm
4. HomeWork2.py - python code containing solutions for HW2

I have implemented a function to track a any colored object (with the help of trackbar settings) in real time as well as just for a single frame. 

The counts mentioned below are very bad, due to the poor and flickering light in the room. If the lighting was good then the count is near about the same but still there are many factors which effect the accuracy i.e. joined objects, same object with intermittent colors etc.


Corresponding object counts for a given algorithm is mentioned in the sample log below:
---------------------------------------------------------------------------------------
0  :  Object Count + contour detection (without morphological operations) => COUNT = 77
1  :  Object Count with Erosion + contour detection  => COUNT = 41
2  :  Object Count with dilation + contour detection  => COUNT = 28
3  :  Object Count with Opening + contour detection  => COUNT = 47
4  :  Object Count with Closing + contour detection  => COUNT = 32
5  :  Object Count with Gradient + contour detection  => COUNT = 98
6  :  Object Count with TopHat + contour detection  => COUNT = 111
7  :  Object Count with BlackHat + contour detection  => COUNT = 121
	
Sample Log:
=============
main: OpenCV version ->  2.4.10
Hello There!!
enter "q" at any stage to quit.
Do you want to process a real time video? (Y/N): 
Please select your options from:
0  :  Object Count + contour detection (without morphological operations)
1  :  Object Count with Erosion + contour detection
2  :  Object Count with dilation + contour detection
3  :  Object Count with Opening + contour detection
4  :  Object Count with Closing + contour detection
5  :  Object Count with Gradient + contour detection
6  :  Object Count with TopHat + contour detection
7  :  Object Count with BlackHat + contour detection
enter "q" to quit.
Enter your selection: 0
0
Performing real time color detection in the video for first 5 frames and taking the average time.
There are total  -1  frames in the video 
O:/WorkSpace/VA_Python/Homework2/Homework2.py:71: FutureWarning: comparison to `None` will result in an element-wise object comparison in the future.
  if(frame == None or not(ret == True)):
Total number of Colored Objects are:  77


Hello There!!
enter "q" at any stage to quit.
Do you want to process a real time video? (Y/N): 
Please select your options from:
0  :  Object Count + contour detection (without morphological operations)
1  :  Object Count with Erosion + contour detection
2  :  Object Count with dilation + contour detection
3  :  Object Count with Opening + contour detection
4  :  Object Count with Closing + contour detection
5  :  Object Count with Gradient + contour detection
6  :  Object Count with TopHat + contour detection
7  :  Object Count with BlackHat + contour detection
enter "q" to quit.
Enter your selection: 1
1
Performing real time color detection in the video for first 5 frames and taking the average time.
There are total  -1  frames in the video 
Total number of Colored Objects are:  41


Hello There!!
enter "q" at any stage to quit.
Do you want to process a real time video? (Y/N): 
Please select your options from:
0  :  Object Count + contour detection (without morphological operations)
1  :  Object Count with Erosion + contour detection
2  :  Object Count with dilation + contour detection
3  :  Object Count with Opening + contour detection
4  :  Object Count with Closing + contour detection
5  :  Object Count with Gradient + contour detection
6  :  Object Count with TopHat + contour detection
7  :  Object Count with BlackHat + contour detection
enter "q" to quit.
Enter your selection: 2
2
Performing real time color detection in the video for first 5 frames and taking the average time.
There are total  -1  frames in the video 
Total number of Colored Objects are:  28


Hello There!!
enter "q" at any stage to quit.
Do you want to process a real time video? (Y/N): 
Please select your options from:
0  :  Object Count + contour detection (without morphological operations)
1  :  Object Count with Erosion + contour detection
2  :  Object Count with dilation + contour detection
3  :  Object Count with Opening + contour detection
4  :  Object Count with Closing + contour detection
5  :  Object Count with Gradient + contour detection
6  :  Object Count with TopHat + contour detection
7  :  Object Count with BlackHat + contour detection
enter "q" to quit.
Enter your selection: 3
3
Performing real time color detection in the video for first 5 frames and taking the average time.
There are total  -1  frames in the video 
Total number of Colored Objects are:  47


Hello There!!
enter "q" at any stage to quit.
Do you want to process a real time video? (Y/N): 
Please select your options from:
0  :  Object Count + contour detection (without morphological operations)
1  :  Object Count with Erosion + contour detection
2  :  Object Count with dilation + contour detection
3  :  Object Count with Opening + contour detection
4  :  Object Count with Closing + contour detection
5  :  Object Count with Gradient + contour detection
6  :  Object Count with TopHat + contour detection
7  :  Object Count with BlackHat + contour detection
enter "q" to quit.
Enter your selection: 4
4
Performing real time color detection in the video for first 5 frames and taking the average time.
There are total  -1  frames in the video 
Total number of Colored Objects are:  32


Hello There!!
enter "q" at any stage to quit.
Do you want to process a real time video? (Y/N): 
Please select your options from:
0  :  Object Count + contour detection (without morphological operations)
1  :  Object Count with Erosion + contour detection
2  :  Object Count with dilation + contour detection
3  :  Object Count with Opening + contour detection
4  :  Object Count with Closing + contour detection
5  :  Object Count with Gradient + contour detection
6  :  Object Count with TopHat + contour detection
7  :  Object Count with BlackHat + contour detection
enter "q" to quit.
Enter your selection: 5
5
Performing real time color detection in the video for first 5 frames and taking the average time.
There are total  -1  frames in the video 
Total number of Colored Objects are:  98


Hello There!!
enter "q" at any stage to quit.
Do you want to process a real time video? (Y/N): 6
Please select your options from:
0  :  Object Count + contour detection (without morphological operations)
1  :  Object Count with Erosion + contour detection
2  :  Object Count with dilation + contour detection
3  :  Object Count with Opening + contour detection
4  :  Object Count with Closing + contour detection
5  :  Object Count with Gradient + contour detection
6  :  Object Count with TopHat + contour detection
7  :  Object Count with BlackHat + contour detection
enter "q" to quit.
Enter your selection: 6
6
Performing real time color detection in the video for first 5 frames and taking the average time.
There are total  -1  frames in the video 
Total number of Colored Objects are:  111


Hello There!!
enter "q" at any stage to quit.
Do you want to process a real time video? (Y/N): 
Please select your options from:
0  :  Object Count + contour detection (without morphological operations)
1  :  Object Count with Erosion + contour detection
2  :  Object Count with dilation + contour detection
3  :  Object Count with Opening + contour detection
4  :  Object Count with Closing + contour detection
5  :  Object Count with Gradient + contour detection
6  :  Object Count with TopHat + contour detection
7  :  Object Count with BlackHat + contour detection
enter "q" to quit.
Enter your selection: 7
7
Performing real time color detection in the video for first 5 frames and taking the average time.
There are total  -1  frames in the video 
Total number of Colored Objects are:  121


Hello There!!
enter "q" at any stage to quit.
Do you want to process a real time video? (Y/N): q

Process finished with exit code 0