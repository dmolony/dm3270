package com.bytezone.dm3270.display;

public interface DisplayScreen
{
  Pen getPen ();

  ScreenPosition getScreenPosition (int position);

  int validate (int position);

  void clearScreen ();

  void insertCursor (int position);
}