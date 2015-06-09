package com.bytezone.dm3270.attributes;

import com.bytezone.dm3270.display.Pen;

public class ResetAttribute extends Attribute
{
  public ResetAttribute (byte value)
  {
    super (AttributeType.RESET, XA_RESET, value);
  }

  @Override
  public void process (Pen pen)
  {
    pen.reset (attributeValue);
  }
}