package org.appspot.apprtc;

import android.os.Environment;
import android.os.SystemClock;

import org.jcodec.codecs.h264.H264Decoder;
import org.jcodec.codecs.h264.io.model.Frame;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.webrtc.EncodedImage;
import org.webrtc.I420BufferImpl;
import org.webrtc.VideoCodecStatus;
import org.webrtc.VideoDecoder;
import org.webrtc.VideoFrame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    dumpEncodedFrame(frame);
    Frame decodedFrame = decoder.decodeFrame(frame.buffer, out.getData());
    dumpDecodedFrame(decodedFrame, frame.captureTimeMs);

    int decodeTimeMs = (int) (SystemClock.elapsedRealtime() - decodeStartTimeMs);
    final VideoFrame.Buffer frameBuffer = wrapI420Buffer(decodedFrame, width, height);
    long presentationTimeNs = frame.captureTimeMs * 1_000_000;
    VideoFrame videoFrame = new VideoFrame(frameBuffer, frame.rotation, presentationTimeNs);

    // Note that qp is parsed on the C++ side.
    callback.onDecodedFrame(videoFrame, decodeTimeMs, null /* qp */);

    return VideoCodecStatus.OK;
  }

  private void dumpEncodedFrame(EncodedImage frame) {
    File dir = new File(Environment.getExternalStorageDirectory(), "webrtc_poc");
    if (!dir.exists()) {
      dir.mkdirs();
    }

    String filename = "enc_" + frame.captureTimeMs + "_"
        + frame.encodedWidth + "x" + frame.encodedHeight + ".h264";
    try {
      FileOutputStream outputStream = new FileOutputStream(new File(dir, filename));

      int position = frame.buffer.position();
      int limit = frame.buffer.limit();
      byte[] data = new byte[limit - position];
      frame.buffer.get(data);
      outputStream.write(data);
      frame.buffer.position(position)
          .limit(limit);

      outputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void dumpDecodedFrame(Frame decodedFrame, long ts) {
    File dir = new File(Environment.getExternalStorageDirectory(), "webrtc_poc");
    if (!dir.exists()) {
      dir.mkdirs();
    }
    int width = decodedFrame.getWidth();
    int height = decodedFrame.getHeight();

    String filename = "dec_" + ts + "_" + width + "x" + height + ".yuv";

    try {
      FileOutputStream outputStream = new FileOutputStream(new File(dir, filename));

      outputStream.write(decodedFrame.getData()[0], 0, width * height);
      outputStream.write(decodedFrame.getData()[1], 0, width * height / 4);
      outputStream.write(decodedFrame.getData()[2], 0, width * height / 4);

      outputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean getPrefersLateDecoding() {
    return false;
  }

  @Override
  public String getImplementationName() {
    return "JCodec decoder";
  }

  private VideoFrame.Buffer wrapI420Buffer(Frame frame, int width, int height) {

    ByteBuffer dataY = ByteBuffer.wrap(frame.getData()[0]);
    dataY.position(0);
    dataY.limit(width * height);

    ByteBuffer dataU = ByteBuffer.wrap(frame.getData()[1]);
    dataU.position(0);
    dataU.limit(width * height / 4);

    ByteBuffer dataV = ByteBuffer.wrap(frame.getData()[2]);
    dataV.position(0);
    dataV.limit(width * height / 4);

    return new I420BufferImpl(width, height, dataY, width, dataU, width / 2, dataV, width / 2, null);
  }
}
