package com.bytezone.dm3270.display;

import com.bytezone.dm3270.attributes.ColorAttribute;

import javafx.scene.paint.Color;

public class ScreenContext
{
  final public Color foregroundColor;
  final public Color backgroundColor;
  final public byte highlight;
  final public boolean highIntensity;

  final public boolean underscore;
  final public boolean reverseVideo;
  final public boolean blink;
  //  final public boolean normalHighlight;

  final ScreenDimensions screenDimensions;
  FontDetails fontDetails;

  public ScreenContext (Color foregroundColor, Color backgroundColor, byte highlight,
      boolean highIntensity, FontDetails fontDetails, ScreenDimensions screenDimensions)
  {
    this.foregroundColor = foregroundColor;
    this.backgroundColor = backgroundColor;
    this.highlight = highlight;
    this.highIntensity = highIntensity;

    this.fontDetails = fontDetails;
    this.screenDimensions = screenDimensions;

    this.underscore = highlight == (byte) 0xF4;
    this.reverseVideo = highlight == (byte) 0xF2;
    this.blink = highlight == (byte) 0xF1;
    //    this.normalHighlight = highlight == (byte) 0xF0;
  }

  public boolean matches (ScreenContext other)
  {
    return foregroundColor == other.foregroundColor
        && backgroundColor == other.backgroundColor     //
        && highlight == other.highlight                 //
        && highIntensity == other.highIntensity;
  }

  public ScreenDimensions getScreenDimensions ()
  {
    return screenDimensions;
  }

  public FontDetails getFontDetails ()
  {
    return fontDetails;
  }

  public void setFontData (FontDetails fontDetails)
  {
    this.fontDetails = fontDetails;
  }

  @Override
  public String toString ()
  {
    return String.format ("[Fg:%-10s Bg:%-10s In:%s  Hl:%02X]",
                          ColorAttribute.getName (foregroundColor),
                          ColorAttribute.getName (backgroundColor),
                          (highIntensity ? 'x' : ' '), highlight);
  }
}