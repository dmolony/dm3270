package com.bytezone.dm3270.attributes;

import com.bytezone.dm3270.display.ScreenContext;

public class ForegroundColor extends ColorAttribute {

  public ForegroundColor(byte value) {
    super(AttributeType.FOREGROUND_COLOR, Attribute.XA_FGCOLOR, value);
  }

  @Override
  public ScreenContext process(ScreenContext defaultContext, ScreenContext currentContext) {
    return currentContext.withForeground(color);
  }

}
