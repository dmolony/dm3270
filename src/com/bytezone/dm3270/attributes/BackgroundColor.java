package com.bytezone.dm3270.attributes;

import com.bytezone.dm3270.display.Pen;

public class BackgroundColor extends ColorAttribute
{
  public BackgroundColor (byte value)
  {
    super (AttributeType.BACKGROUND_COLOR, Attribute.XA_BGCOLOR, value);
  }

  @Override
  public void process (Pen pen)
  {
    pen.setBackground (color);
  }
}