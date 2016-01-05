This zip file contains solution to Homework5.

Please refer to RealTimeActionRecognition.py & RealTimeActionRecognition_3skips.py (with 3 type of frame skip count combinations i.e. 1, 7, 14) for the solution.

I have used Different Features extraction algorithms and combinations 
(i.e. Detector-Extractor e.g. SIFT-SIFT, DENSESIFT-SIFT,SURF-SURF, SIFT-SURF etc), with SVM as classifier 
and BagOfWords strategy on Motion History Images for all the videos.

I had got different level of accuracies in the range or 40-80%
--------------------------------------------------------------

The best accuracy I got was with SURF-SURF combination with current motion history settings 
i.e. MHI_DURATION = 1.0,DEFAULT_THRESHOLD = 40,MAX_TIME_DELTA = 0.5,MIN_TIME_DELTA = 0.05 

Accuracy of the current program is approx 70%.
----------------------------------------------

For running the program, please extract the zip file conents in the same folder and store all the datasets (in their respective folders) in a folder named dataset-1

Thank you.