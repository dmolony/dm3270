package com.bytezone.dm3270.attributes;

import com.bytezone.dm3270.display.ContextManager;
import com.bytezone.dm3270.display.ScreenContext;

public class ResetAttribute extends Attribute
{
  public ResetAttribute (byte value)
  {
    super (AttributeType.RESET, XA_RESET, value);
  }

  @Override
  public ScreenContext process (ContextManager contextManager,
      ScreenContext defaultContext, ScreenContext currentContext)
  {
    return defaultContext;
  }
}