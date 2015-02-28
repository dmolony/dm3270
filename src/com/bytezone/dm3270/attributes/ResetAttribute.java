package com.bytezone.dm3270.attributes;

import com.bytezone.dm3270.application.ScreenPosition;
import com.bytezone.dm3270.display.ContextHandler;
import com.bytezone.dm3270.display.ScreenContext;

public class ResetAttribute extends Attribute
{
  public ResetAttribute (byte value)
  {
    super (AttributeType.RESET, XA_RESET, value);
  }

  @Override
  public void process (ScreenPosition screenPosition)
  {
    // this should probably deal with the field as the SP has to revert to a
    // previous value
    screenPosition.resetAttribute ();
  }

  @Override
  public ScreenContext
      process (ContextHandler contextHandler, ScreenContext screenContext)
  {
    System.out.println ("Not changing context in ResetAttribute");
    return screenContext;
  }
}