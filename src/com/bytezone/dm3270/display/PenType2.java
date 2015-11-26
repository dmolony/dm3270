package com.bytezone.dm3270.display;

import java.util.Iterator;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;

import javafx.scene.paint.Color;

public class PenType2 implements Pen
{

  @Override
  public Iterator<ScreenPosition> iterator ()
  {
    return null;
  }

  @Override
  public ScreenContext getDefaultScreenContext ()
  {
    return null;
  }

  @Override
  public void reset ()
  {
  }

  @Override
  public void startField (StartFieldAttribute startFieldAttribute)
  {
  }

  @Override
  public void addAttribute (Attribute attribute)
  {
  }

  @Override
  public int getPosition ()
  {
    return 0;
  }

  @Override
  public void setForeground (Color color)
  {
  }

  @Override
  public void setBackground (Color color)
  {
  }

  @Override
  public void setHighlight (byte value)
  {
  }

  @Override
  public void setHighIntensity (boolean value)
  {
  }

  @Override
  public void reset (byte value)
  {
  }

  @Override
  public void writeGraphics (byte b)
  {
  }

  @Override
  public void write (byte b)
  {
  }

  @Override
  public void moveRight ()
  {
  }

  @Override
  public void eraseEOF ()
  {
  }

  @Override
  public void tab ()
  {
  }

  @Override
  public void moveTo (int position)
  {
  }

  @Override
  public String getScreenText (int columns)
  {
    return null;
  }

  @Override
  public int validate (int position)
  {
    return 0;
  }

}
