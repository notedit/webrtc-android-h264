package org.appspot.apprtc;

import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.common.VideoEncoder.EncodedFrame;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.webrtc.EncodedImage;
import org.webrtc.VideoCodecStatus;
import org.webrtc.VideoEncoder;
import org.webrtc.VideoFrame;

import java.nio.ByteBuffer;

/**
 * Created by Piasy{github.com/Piasy} on 15/08/2017.
 */

public class SwAvcEncoder implements VideoEncoder {
  private Callback callback;
  private H264Encoder encoder;
  private Picture outPic;
  private ByteBuffer outBuf;
  private int width;
  private int height;

  @Override
  public VideoCodecStatus initEncode(Settings settings, Callback encodeCallback) {
    width = settings.width;
    height = settings.height;
    outPic = Picture.create(width, height, ColorSpace.YUV420J);
    outBuf = ByteBuffer.allocateDirect(width * height * 3 / 2);
    callback = encodeCallback;
    encoder = H264Encoder.createH264Encoder();
    return VideoCodecStatus.OK;
  }

  @Override
  public VideoCodecStatus release() {
    return VideoCodecStatus.OK;
  }

  @Override
  public VideoCodecStatus encode(VideoFrame frame, EncodeInfo info) {
    fillImage(outPic, frame.getBuffer().toI420());
    EncodedFrame encodedFrame = encoder.encodeFrame(outPic, outBuf);
    callback.onEncodedFrame(
        EncodedImage.builder()
            .setCaptureTimeNs(frame.getTimestampNs())
            .setCompleteFrame(true)
            .setEncodedWidth(frame.getBuffer().getWidth())
            .setEncodedHeight(frame.getBuffer().getHeight())
            .setRotation(frame.getRotation())
            .setBuffer(encodedFrame.getData())
            .setFrameType(encodedFrame.isKeyFrame()
                ? EncodedImage.FrameType.VideoFrameKey
                : EncodedImage.FrameType.VideoFrameDelta)
            .createEncodedImage(),
        new CodecSpecificInfo());
    return VideoCodecStatus.OK;
  }

  private void fillImage(Picture picture, VideoFrame.I420Buffer i420Buffer) {
    byte[][] data = picture.getData();
    ByteBuffer y = i420Buffer.getDataY();
    y.position(0)
        .limit(y.capacity());
    y.get(data[0]);

    ByteBuffer u = i420Buffer.getDataY();
    u.position(0)
        .limit(u.capacity());
    u.get(data[0]);

    ByteBuffer v = i420Buffer.getDataY();
    v.position(0)
        .limit(v.capacity());
    v.get(data[0]);
  }

  @Override
  public VideoCodecStatus setChannelParameters(short packetLoss, long roundTripTimeMs) {
    return VideoCodecStatus.OK;
  }

  @Override
  public VideoCodecStatus setRateAllocation(BitrateAllocation allocation, int framerate) {
    return VideoCodecStatus.OK;
  }

  @Override
  public ScalingSettings getScalingSettings() {
    return new ScalingSettings(false);
  }

  @Override
  public String getImplementationName() {
    return "JCodec encoder";
  }
}
