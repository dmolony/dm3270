package com.bytezone.dm3270.display;

import javafx.scene.paint.Color;

public class ScreenContext
{
  final public Color foregroundColor;
  final public Color backgroundColor;
  final public boolean underscore;
  final public boolean reverseVideo;
  final public boolean blink;
  final public boolean normalHighlight;
  final public boolean highIntensity;
  final public byte highlight;

  public ScreenContext (Color foregroundColor, Color backgroundColor, byte highlight,
      boolean highIntensity)
  {
    this.foregroundColor = foregroundColor;
    this.backgroundColor = backgroundColor;
    this.underscore = highlight == (byte) 0xF4;
    this.reverseVideo = highlight == (byte) 0xF2;
    this.blink = highlight == (byte) 0xF1;
    this.normalHighlight = highlight == (byte) 0xF0;
    this.highlight = highlight;
    this.highIntensity = highIntensity;
  }

  public boolean matches (ScreenContext other)
  {
    return foregroundColor == other.foregroundColor
        && backgroundColor == other.backgroundColor     //
        && highlight == other.highlight                 //
        && highIntensity == other.highIntensity;
  }

  @Override
  public String toString ()
  {
    return String.format ("[Fg:%s Bg:%s In:%s Hl:%02X]", foregroundColor,
                          backgroundColor, (highIntensity ? 'x' : ' '), highlight);
  }
}