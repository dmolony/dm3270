package com.bytezone.dm3270.attributes;

import com.bytezone.dm3270.display.ContextManager;
import com.bytezone.dm3270.display.Pen;
import com.bytezone.dm3270.display.ScreenContext;

public class ForegroundColor extends ColorAttribute
{
  public ForegroundColor (byte value)
  {
    super (AttributeType.FOREGROUND_COLOR, Attribute.XA_FGCOLOR, value);
  }

  @Override
  public ScreenContext
      process (ContextManager contextHandler, ScreenContext screenContext)
  {
    return contextHandler.setForeground (screenContext, color);
  }

  @Override
  public void process (Pen pen)
  {
    pen.setForeground (color);
  }
}