package com.bytezone.dm3270.attributes;

import com.bytezone.dm3270.display.ContextManager;
import com.bytezone.dm3270.display.ScreenContext;

public class ForegroundColor extends ColorAttribute
{
  public ForegroundColor (byte value)
  {
    super (AttributeType.FOREGROUND_COLOR, Attribute.XA_FGCOLOR, value);
  }

  @Override
  public ScreenContext process (ContextManager contextManager,
      ScreenContext defaultContext, ScreenContext currentContext)
  {
    return contextManager.setForeground (currentContext, color);
  }
}