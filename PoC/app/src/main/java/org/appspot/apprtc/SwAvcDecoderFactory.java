package org.appspot.apprtc;

import org.webrtc.EglBase;
import org.webrtc.VideoDecoder;
import org.webrtc.VideoDecoderFactory;

/**
 * Created by Piasy{github.com/Piasy} on 15/08/2017.
 */

public class SwAvcDecoderFactory implements VideoDecoderFactory {

  public SwAvcDecoderFactory(EglBase.Context eglContext) {
  }

  @Override
  public VideoDecoder createDecoder(String codecType) {
    switch (codecType) {
      case "H264":
        return new SwAvcDecoder();
      case "VP8":
      case "VP9":
      default:
        return null;
    }
  }
}
