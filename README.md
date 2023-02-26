# GarbageMapperDetector
> Android mobile app written in Java using pre-trained YOLO model for detecting garbage.

## Table of contents
* [Location package](#location-package)
* [Detection package](#detection-package)
* [Helpers package](#helpers-package)


## Location package
Location Java package contains 2 classes and 1 interface:
- FallbackLocationTracker
- ProviderLocationTracker
- LocationTracker

Each of them are responsible for handling retrieving of the current location using all available providers like network and gps.

## Detection package
Detection package contains 2 classes:
| Class | Description |
| ----------- | ----------- |
| BaseModuleActivity | Responsible for handling threads in background of running detection model |
| ObjectDetectionActivity | Compresses current frame of stream video to bitmap and tensor for model input, then it detects objects, using PrePostProcessor runs non-max suppresion with post processing and as the final step, it sends data of detected objects to Firebase | 


## Helpers package
Helpers package contains 3 classes:
| Class | Description |
| ----------- | ----------- | 
| AbstractCameraXActivity | Responsible for handling work of camera plugin |
| PrePostProcessor | Implements non-max suppresion, all objects whose probability is greater than the threshold, their data are sent to Firebase |
| ResultView | Draws bounding-boxes on a current frame |

