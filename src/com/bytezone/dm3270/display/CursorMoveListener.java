package com.bytezone.dm3270.display;

public interface CursorMoveListener
{
  public abstract void cursorMoved (int oldLocation, int newLocation, Field field);
}