package com.bytezone.dm3270.attributes;

import com.bytezone.dm3270.display.Pen;

public class ForegroundColor extends ColorAttribute
{
  public ForegroundColor (byte value)
  {
    super (AttributeType.FOREGROUND_COLOR, Attribute.XA_FGCOLOR, value);
  }

  @Override
  public void process (Pen pen)
  {
    pen.setForeground (color);
  }
}