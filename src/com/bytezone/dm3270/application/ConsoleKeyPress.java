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
  private static final KeyCode[] PFKeyCodes = //
      { KeyCode.F1, KeyCode.F2, KeyCode.F3, KeyCode.F4, KeyCode.F5, KeyCode.F6,
       KeyCode.F7, KeyCode.F8, KeyCode.F9, KeyCode.F10, KeyCode.F11, KeyCode.F12 };

  private final Screen screen;
  private final ConsolePane consolePane;
  private final Cursor cursor;

  public ConsoleKeyPress (ConsolePane consolePane, Screen screen)
  {
    this.consolePane = consolePane;
    this.screen = screen;
    this.cursor = screen.getScreenCursor ();
  }

  @Override
  public void handle (KeyEvent e)
  {
    KeyCode keyCodePressed = e.getCode ();

    if (screen.isKeyboardLocked ())
    {
      e.consume ();       // always consume it when the keyboard is locked

      if (e.isControlDown ())       // should allow user to choose modifier key
        if (keyCodePressed == KeyCode.LEFT)
          consolePane.back ();
        else if (keyCodePressed == KeyCode.RIGHT)
          consolePane.forward ();

      return;
    }

    if (e.isControlDown ())       // should allow user to choose modifier key
    {
      switch (keyCodePressed)
      {
        case ENTER:
          cursor.newLine ();
          e.consume ();
          break;

        case BACK_SPACE:
        case DELETE:
          cursor.eraseEOL ();
          e.consume ();
          break;

        case H:
          cursor.home ();
          e.consume ();
          break;

        case I:
          screen.toggleInsertMode ();
          e.consume ();
          break;

        case F1:
          consolePane.sendAID (AIDCommand.AID_PA1, "PA1");
          e.consume ();
          break;

        case F2:
          consolePane.sendAID (AIDCommand.AID_PA2, "PA2");
          e.consume ();
          break;

        case F3:
          consolePane.sendAID (AIDCommand.AID_PA3, "PA3");
          e.consume ();
          break;

        default:
          break;
      }
      return;
    }

    if (keyCodePressed.isArrowKey ())
      switch (keyCodePressed)
      {
        case LEFT:
          cursor.move (Direction.LEFT);
          e.consume ();
          break;

        case RIGHT:
          cursor.move (Direction.RIGHT);
          e.consume ();
          break;

        case UP:
          cursor.move (Direction.UP);
          e.consume ();
          break;

        case DOWN:
          cursor.move (Direction.DOWN);
          e.consume ();
          break;

        default:
          System.out.println ("Impossible");
          break;
      }
    else
      switch (keyCodePressed)
      {
        case ENTER:
          consolePane.sendAID (AIDCommand.AID_ENTER_KEY, "ENTR");
          e.consume ();
          break;

        case TAB:
          cursor.tab (e.isShiftDown ());
          e.consume ();
          break;

        case BACK_SPACE:
          cursor.backspace ();
          e.consume ();
          break;

        case DELETE:
          cursor.delete ();
          e.consume ();
          break;

        case END:
          cursor.eraseEOL ();
          e.consume ();
          break;

        case INSERT:
          screen.toggleInsertMode ();
          e.consume ();
          break;

        case HOME:
          cursor.home ();
          e.consume ();
          break;

        case ESCAPE:
          System.out.println ("escape");    // CLR key?
          e.consume ();
          break;

        default:
          boolean found = false;
          int pfKey = 1;
          for (KeyCode keyCode : PFKeyCodes)
          {
            if (keyCode == keyCodePressed)
            {
              found = true;
              break;
            }
            ++pfKey;
          }
          if (found)
          {
            if (e.isShiftDown ())
              pfKey += 12;
            String keyName = "PF" + pfKey;
            consolePane.sendAID (AIDCommand.getKey (keyName), keyName);
            e.consume ();
          }
          break;
      }
  }
}