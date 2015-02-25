package com.bytezone.dm3270.application;

import javafx.scene.paint.Color;

public class ScreenContext
{
  final public Color foregroundColor;
  final public Color backgroundColor;
  final public boolean underscore;
  final public boolean reverseVideo;
  final public boolean highIntensity;

  public ScreenContext (Color foregroundColor, Color backgroundColor, boolean underscore,
      boolean reverseVideo, boolean highIntensity)
  {
    this.foregroundColor = foregroundColor;
    this.backgroundColor = backgroundColor;
    this.underscore = underscore;
    this.reverseVideo = reverseVideo;
    this.highIntensity = highIntensity;
  }

  public boolean matches (ScreenContext other)
  {
    return foregroundColor == other.foregroundColor
        && backgroundColor == other.backgroundColor     //
        && underscore == other.underscore               //
        && reverseVideo == other.reverseVideo           //
        && highIntensity == other.highIntensity;
  }

  @Override
  public String toString ()
  {
    return String.format ("[Fg:%s Bg:%s Un:%s Hi:%s Rv:%s]", foregroundColor,
                          backgroundColor, (underscore ? 'x' : ' '), (highIntensity ? 'x'
                              : ' '), (reverseVideo ? 'x' : ' '));
  }
}