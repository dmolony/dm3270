package com.bytezone.dm3270.display;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;

public interface Pen extends Iterable<ScreenPosition>
{
  //  ScreenContext getDefaultScreenContext ();

  void clearScreen ();

  void startField (StartFieldAttribute startFieldAttribute);

  void addAttribute (Attribute attribute);

  int getPosition ();

  //  void setForeground (Color color);
  //
  //  void setBackground (Color color);
  //
  //  void setHighlight (byte value);
  //
  //  void setHighIntensity (boolean value);
  //
  //  void reset (byte value);

  void writeGraphics (byte b);

  void write (byte b);

  void moveRight ();

  void eraseEOF ();

  void tab ();

  void moveTo (int position);

  String getScreenText (int columns);     // remove columns later

  public int validate (int position);
}