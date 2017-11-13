package org.appspot.apprtc;

import org.webrtc.EglBase;
import org.webrtc.VideoCodecInfo;
import org.webrtc.VideoEncoder;
import org.webrtc.VideoEncoderFactory;

import java.util.Collections;

/**
 * Created by Piasy{github.com/Piasy} on 15/08/2017.
 */

public class SwAvcEncoderFactory implements VideoEncoderFactory {

  public SwAvcEncoderFactory(EglBase.Context eglContext) {
  }

  @Override
  public VideoEncoder createEncoder(VideoCodecInfo info) {
    switch (info.name) {
      case "H264":
        return new SwAvcEncoder();
      case "VP8":
      case "VP9":
      default:
        return null;
    }
  }

  @Override
  public VideoCodecInfo[] getSupportedCodecs() {
    return new VideoCodecInfo[]{
        new VideoCodecInfo(0, "H264", Collections.emptyMap())
    };
  }
}
