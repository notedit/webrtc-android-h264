# webrtc-android-h264

android support MediaCodec h264 encode/decode, but not every device is supported
we make this project to add soft h264 encode/decode support to webrtc android sdk



## todos


- support h264 decode 
- support h264 encode 



### step1 

soft h264 decode support, webrtc's api did not support external encode facotry yet, so we just support external decode factory.

we may use jcodec to do h264 decode in the step1.  we need implement an decode factory and an soft decoder

https://github.com/pristineio/webrtc-mirror/blob/master/webrtc/sdk/android/src/java/org/webrtc/HardwareVideoDecoder.java


### step2 

soft h264 encode support, we will see when webrtc will support external encode factory. 

the same logic as step1


### step3 

we will use openh264 or ffmpeg to replace jcodec.




