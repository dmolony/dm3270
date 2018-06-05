package com.bytezone.dm3270.display;

public interface CursorMoveListener {

  void cursorMoved(int oldLocation, int newLocation, Field field);

}
