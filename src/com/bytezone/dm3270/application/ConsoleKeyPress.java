package com.bytezone.dm3270.application;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.Cursor.Direction;
import com.bytezone.dm3270.display.Screen;

class ConsoleKeyPress implements EventHandler<KeyEvent>
{
  private static final KeyCode[] keyCodes = //
      { KeyCode.F1, KeyCode.F2, KeyCode.F3, KeyCode.F4, KeyCode.F5, KeyCode.F6,
       KeyCode.F7, KeyCode.F8, KeyCode.F9, KeyCode.F10, KeyCode.F11, KeyCode.F12 };
  private static final byte[] keyValues = //
      { (byte) 0xF1, (byte) 0xF2, (byte) 0xF3, (byte) 0xF4, (byte) 0xF5, (byte) 0xF6,
       (byte) 0xF7, (byte) 0xF8, (byte) 0xF9, (byte) 0x7A, (byte) 0x7B, (byte) 0x7C,
       (byte) 0xC1, (byte) 0xC2, (byte) 0xC3, (byte) 0xC4, (byte) 0xC5, (byte) 0xC6,
       (byte) 0xC7, (byte) 0xC8, (byte) 0xC9, (byte) 0x4A, (byte) 0x4B, (byte) 0x4C, };

  private final Screen screen;
  private final ConsoleStage console;
  private final Cursor cursor;

  public ConsoleKeyPress (ConsoleStage console, Screen screen)
  {
    this.console = console;
    this.screen = screen;
    this.cursor = screen.getScreenCursor ();
  }

  @Override
  public void handle (KeyEvent e)
  {
    if (e.isMetaDown ())
      return;

    if (screen.isKeyboardLocked ())
    {
      System.out.println ("Locked keyboard - ignoring : " + e);
      return;
    }

    KeyCode keyCodePressed = e.getCode ();

    switch (keyCodePressed)
    {
      case ENTER:
        screen.setAID ((byte) 0x7D);
        AIDCommand command = new AIDCommand (screen);
        console.sendData (command.getTelnetData ());
        break;

      case TAB:
        cursor.tab (e.isShiftDown ());
        break;

      case LEFT:
        cursor.move (Direction.LEFT);
        break;
      case RIGHT:
        cursor.move (Direction.RIGHT);
        break;
      case UP:
        cursor.move (Direction.UP);
        break;
      case DOWN:
        cursor.move (Direction.DOWN);
        break;

      case BACK_SPACE:
        cursor.backspace ();
        break;

      case DELETE:
        System.out.println ("delete");
        break;

      case ESCAPE:
        System.out.println ("escape");    // CLR key?
        break;

      default:
        boolean found = false;
        int pfKey = 0;
        for (KeyCode keyCode : keyCodes)
        {
          ++pfKey;
          if (keyCode == keyCodePressed)
          {
            found = true;
            break;
          }
        }
        if (found)
        {
          if (e.isShiftDown ())
            pfKey += 12;

          screen.setAID (keyValues[pfKey]);
          command = new AIDCommand (screen);
          console.sendData (command.getTelnetData ());
        }
        break;
    }
  }
}