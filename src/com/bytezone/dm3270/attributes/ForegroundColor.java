package com.bytezone.dm3270.attributes;

import com.bytezone.dm3270.application.ScreenPosition;

public class ForegroundColor extends ColorAttribute
{
  public ForegroundColor (byte value)
  {
    super (AttributeType.FOREGROUND_COLOR, Attribute.XA_FGCOLOR, value);
  }

  @Override
  public void process (ScreenPosition screenPosition)
  {
    screenPosition.setForeground (colors[attributeValue & 0x0F]);
  }
}