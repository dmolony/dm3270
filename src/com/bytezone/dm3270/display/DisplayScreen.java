package com.bytezone.dm3270.display;

public interface DisplayScreen
{
  public Pen getPen ();

  public ScreenPosition getScreenPosition (int position);

  public int validate (int position);

  public void clearScreen ();

  public void insertCursor (int position);
}