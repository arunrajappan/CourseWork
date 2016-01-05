This submission has following files:

1. ColorMarkedHSV-out*.jpg - Grabbed HSV images of the red ball/red cap
2. ColorMarked-out*.jpg - Grabbed color detected images of the red ball/red cap in the video
3. out*.jpg - Grabbed images from the videos
4. HomeWork1.py - python code containing solutions for HW1

Average time take for Color detection in subsequent 5 frames for saved/offline video was: 
1. 0:00:00.007600 (hh:mm:ss:ms)
2. 0:00:00.407000  (hh:mm:ss:ms)
Average processing time for Color detection in subsequent 5 frames for real time video was  0:00:00.147800 (hh:mm:ss:ms)

I have implemented a function to track a red object in real time and my observations are as mentioned below: 
	Color detection can be done in a real time video but the frame rate has to be as per speed of the object to be tracked, in order to track the object accurately or else there be loss of some frames or the object is moving too fast then it cannot be tracked. Hence I was only able to track the object if it was moving slow.