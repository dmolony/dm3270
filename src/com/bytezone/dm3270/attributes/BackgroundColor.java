package com.bytezone.dm3270.attributes;

import com.bytezone.dm3270.display.ContextManager;
import com.bytezone.dm3270.display.ScreenContext;

public class BackgroundColor extends ColorAttribute
{
  public BackgroundColor (byte value)
  {
    super (AttributeType.BACKGROUND_COLOR, Attribute.XA_BGCOLOR, value);
  }

  @Override
  public ScreenContext process (ContextManager contextManager,
      ScreenContext defaultContext, ScreenContext currentContext)
  {
    return contextManager.setBackground (currentContext, color);
  }
}