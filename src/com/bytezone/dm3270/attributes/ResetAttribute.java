package com.bytezone.dm3270.attributes;

import com.bytezone.dm3270.display.ScreenContext;

public class ResetAttribute extends Attribute
{
  public ResetAttribute (byte value)
  {
    super (AttributeType.RESET, XA_RESET, value);
  }

  //  @Override
  //  public void process (Pen pen)
  //  {
  //    pen.reset (attributeValue);
  //  }

  @Override
  public ScreenContext process (ScreenContext defaultContext,
      ScreenContext currentContext)
  {
    return contextManager.setHighlight (currentContext, defaultContext.highlight);
  }
}