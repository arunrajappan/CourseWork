__author__ = 'Arunkumar'


import numpy as np
import cv2
# import matplotlib
from matplotlib import pyplot as plt

# x = np.random.randint(25,100,25)
# y = np.random.randint(1705,255,25)
# z = np.hstack((x,y))
# z = z.reshape((50,1))
# z = np.float32(z)
# plt.hist(z,256,[0,256]),plt.show()
myArr = np.float32([[1,2,3]])
myArr2 = np.float32([[1,2,4]])
print(myArr)
print(np.sum([myArr,myArr2],axis=0))