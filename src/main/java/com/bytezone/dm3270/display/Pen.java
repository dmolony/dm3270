package com.bytezone.dm3270.display;

import com.bytezone.dm3270.Charset;
import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;

public interface Pen extends Iterable<ScreenPosition> {

  static Pen getInstance(ScreenPosition[] screenPositions, ScreenDimensions screenDimensions,
      Charset charset) {
    return new PenType1(screenPositions, screenDimensions, charset);
  }

  void clearScreen();

  void startField(StartFieldAttribute startFieldAttribute);

  void addAttribute(Attribute attribute);

  int getPosition();

  void writeGraphics(byte b);

  void write(byte b);

  void moveRight();

  void moveToNextLine();

  void eraseEOF();

  void tab();

  void moveTo(int position);

  int validate(int position);

  void setScreenDimensions(ScreenDimensions screenDimensions);

  Iterable<ScreenPosition> fromCurrentPosition();

}
