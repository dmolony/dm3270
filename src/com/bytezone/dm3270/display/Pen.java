package com.bytezone.dm3270.display;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;

public interface Pen extends Iterable<ScreenPosition>
{
  void clearScreen ();

  void startField (StartFieldAttribute startFieldAttribute);

  void addAttribute (Attribute attribute);

  int getPosition ();

  void writeGraphics (byte b);

  void write (byte b);

  void moveRight ();

  void eraseEOF ();

  void tab ();

  void moveTo (int position);

  String getScreenText ();

  public int validate (int position);
}