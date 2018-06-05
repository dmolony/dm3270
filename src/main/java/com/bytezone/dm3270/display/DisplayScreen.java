package com.bytezone.dm3270.display;

public interface DisplayScreen {

  Pen getPen();

  ScreenDimensions getScreenDimensions();

  ScreenPosition getScreenPosition(int position);

  int validate(int position);

  void clearScreen();

  void insertCursor(int position);

}
