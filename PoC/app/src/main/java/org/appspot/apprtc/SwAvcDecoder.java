package org.appspot.apprtc;

import android.os.SystemClock;

import org.jcodec.codecs.h264.H264Decoder;
import org.jcodec.codecs.h264.io.model.Frame;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.webrtc.EncodedImage;
import org.webrtc.JavaI420Buffer;
import org.webrtc.VideoCodecStatus;
import org.webrtc.VideoDecoder;
import org.webrtc.VideoFrame;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Piasy{github.com/Piasy} on 15/08/2017.
 */

public class SwAvcDecoder implements VideoDecoder {
  private ExecutorService decoderExecutor;
  private Callback callback;
  private H264Decoder decoder;
  private Picture out;
  private int width;
  private int height;

  @Override
  public VideoCodecStatus initDecode(Settings settings, Callback decodeCallback) {
    decoder = new H264Decoder();
    callback = decodeCallback;
    out = Picture.create(settings.width, settings.height, ColorSpace.YUV420J);
    width = settings.width;
    height = settings.height;
    decoderExecutor = Executors.newSingleThreadExecutor();
    return VideoCodecStatus.OK;
  }

  @Override
  public VideoCodecStatus release() {
    decoderExecutor.shutdownNow();
    return VideoCodecStatus.OK;
  }

  @Override
  public VideoCodecStatus decode(EncodedImage frame, DecodeInfo info) {
    long decodeStartTimeMs = SystemClock.elapsedRealtime();

    Frame decodedFrame = decoder.decodeFrame(frame.buffer, out.getData());

    int decodeTimeMs = (int) (SystemClock.elapsedRealtime() - decodeStartTimeMs);
    final VideoFrame.Buffer frameBuffer = copyI420Buffer(decodedFrame, width, height);
    long presentationTimeNs = frame.captureTimeMs * 1_000_000;
    VideoFrame videoFrame = new VideoFrame(frameBuffer, frame.rotation, presentationTimeNs);

    // Note that qp is parsed on the C++ side.
    callback.onDecodedFrame(videoFrame, decodeTimeMs, null /* qp */);

    return VideoCodecStatus.OK;
  }

  @Override
  public boolean getPrefersLateDecoding() {
    return true;
  }

  @Override
  public String getImplementationName() {
    // if only return JCodec decoder,
    // signaling_thread crash with error:
    // JNI DETECTED ERROR IN APPLICATION:
    // input is not valid Modified UTF-8:
    // illegal continuation byte 0xff'
    // but no stack symbol available :(
    return "JCodec decoder OMX.qcom.video.decoder.avc";
    //return "JCodec decoder";
  }

  private VideoFrame.Buffer copyI420Buffer(Frame frame, int width, int height) {
    VideoFrame.I420Buffer frameBuffer = JavaI420Buffer.allocate(width, height);

    ByteBuffer dataY = frameBuffer.getDataY();
    dataY.put(frame.getData()[0], 0, width * height);

    ByteBuffer dataU = frameBuffer.getDataU();
    dataU.put(frame.getData()[1], 0, width * height / 4);

    ByteBuffer dataV = frameBuffer.getDataV();
    dataV.put(frame.getData()[2], 0, width * height / 4);

    return frameBuffer;
  }
}
