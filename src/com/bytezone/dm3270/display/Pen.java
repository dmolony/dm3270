package com.bytezone.dm3270.display;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;

import javafx.scene.canvas.GraphicsContext;

public interface Pen extends Iterable<ScreenPosition>
{
  static Pen getInstance (ScreenPosition[] screenPositions, GraphicsContext gc,
      ContextManager contextManager, ScreenDimensions screenDimensions)
  {
    return new PenType1 (screenPositions, gc, contextManager, screenDimensions);
  }

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

  public void setScreenDimensions (ScreenDimensions screenDimensions);
}