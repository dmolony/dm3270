package com.bytezone.dm3270.attributes;

import javafx.scene.paint.Color;

import com.bytezone.dm3270.application.ScreenPosition;
import com.bytezone.dm3270.display.ContextManager;
import com.bytezone.dm3270.display.ScreenContext;

public class BackgroundColor extends ColorAttribute
{
  public BackgroundColor (byte value)
  {
    super (AttributeType.BACKGROUND_COLOR, Attribute.XA_BGCOLOR, value);
  }

  @Override
  public void process (ScreenPosition screenPosition)
  {
    screenPosition.setBackground (colors[attributeValue & 0x0F]);
  }

  @Override
  public Color getColor ()
  {
    return colors[attributeValue & 0x0F];
  }

  @Override
  public ScreenContext
      process (ContextManager contextHandler, ScreenContext screenContext)
  {
    return contextHandler.setBackground (screenContext, colors[attributeValue & 0x0F]);
  }
}