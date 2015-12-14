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

  FontDetails fontDetails;

  public ScreenContext (Color foregroundColor, Color backgroundColor, byte highlight,
      boolean highIntensity, FontDetails fontDetails)
  {
    this.foregroundColor = foregroundColor;
    this.backgroundColor = backgroundColor;
    this.highlight = highlight;
    this.highIntensity = highIntensity;

    this.fontDetails = fontDetails;

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

  public boolean matches (Color foregroundColor, Color backgroundColor, byte highlight,
      boolean highIntensity)
  {
    return this.foregroundColor == foregroundColor
        && this.backgroundColor == backgroundColor     //
        && this.highlight == highlight                 //
        && this.highIntensity == highIntensity;
  }

  public void setFontDetails (FontDetails fontDetails)
  {
    this.fontDetails = fontDetails;
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
    String name = fontDetails == null ? "" : fontDetails.font.getName ();
    return String.format ("[Fg:%-10s Bg:%-10s In:%s  Hl:%02X, f:%s]",
                          ColorAttribute.getName (foregroundColor),
                          ColorAttribute.getName (backgroundColor),
                          (highIntensity ? 'x' : ' '), highlight, name);
  }
}