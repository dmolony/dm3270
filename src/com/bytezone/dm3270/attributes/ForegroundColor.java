package com.bytezone.dm3270.attributes;

import javafx.scene.paint.Color;

import com.bytezone.dm3270.display.ContextManager;
import com.bytezone.dm3270.display.ScreenContext;

public class ForegroundColor extends ColorAttribute
{
  public ForegroundColor (byte value)
  {
    super (AttributeType.FOREGROUND_COLOR, Attribute.XA_FGCOLOR, value);
  }

  //  @Override
  //  public void process (ScreenPosition screenPosition)
  //  {
  //    screenPosition.setForeground (colors[attributeValue & 0x0F]);
  //  }

  @Override
  public Color getColor ()
  {
    return colors[attributeValue & 0x0F];
  }

  @Override
  public ScreenContext
      process (ContextManager contextHandler, ScreenContext screenContext)
  {
    return contextHandler.setForeground (screenContext, colors[attributeValue & 0x0F]);
  }
}