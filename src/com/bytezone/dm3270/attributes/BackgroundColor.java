package com.bytezone.dm3270.attributes;

import com.bytezone.dm3270.display.ContextManager;
import com.bytezone.dm3270.display.Pen;
import com.bytezone.dm3270.display.ScreenContext;

public class BackgroundColor extends ColorAttribute
{
  public BackgroundColor (byte value)
  {
    super (AttributeType.BACKGROUND_COLOR, Attribute.XA_BGCOLOR, value);
  }

  @Override
  public ScreenContext
      process (ContextManager contextHandler, ScreenContext screenContext)
  {
    return contextHandler.setBackground (screenContext, color);
  }

  @Override
  public void process (Pen pen)
  {
    pen.setBackground (color);
  }
}