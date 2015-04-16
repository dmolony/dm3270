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
  private final String os = System.getProperty ("os.name");
  private final boolean isMac = os != null && os.startsWith ("Mac");

  private final Screen screen;
  private final ConsoleStage consoleStage;
  private final Cursor cursor;

  public ConsoleKeyPress (ConsoleStage console, Screen screen)
  {
    this.consoleStage = console;
    this.screen = screen;
    this.cursor = screen.getScreenCursor ();
  }

  @Override
  public void handle (KeyEvent e)
  {
    e.consume ();

    KeyCode keyCodePressed = e.getCode ();

    if (e.isMetaDown ())
    {
      if (keyCodePressed == KeyCode.LEFT)
      {
        consoleStage.back ();
        return;
      }
      if (keyCodePressed == KeyCode.RIGHT)
      {
        consoleStage.forward ();
        return;
      }
    }

    if (screen.isKeyboardLocked ())
      return;

    if (e.isControlDown ())       // should allow user to choose modifier key
    {
      switch (keyCodePressed)
      {
        case ENTER:
          cursor.newLine ();
          break;

        case TAB:
          cursor.newLine ();
          break;

        case BACK_SPACE:
          if (isMac)
            cursor.eraseEOL ();
          break;

        case H:
          if (isMac)
            cursor.home ();
          break;

        case I:
          if (isMac)
            screen.toggleInsertMode ();
          break;

        case F1:
          sendAID (AIDCommand.AID_PA1);
          break;

        case F2:
          sendAID (AIDCommand.AID_PA2);
          break;

        case F3:
          sendAID (AIDCommand.AID_PA3);
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

        default:
          System.out.println ("Impossible");
          break;
      }
    else
      switch (keyCodePressed)
      {
        case ENTER:
          sendAID (AIDCommand.AID_ENTER_KEY);
          break;

        case TAB:
          cursor.tab (e.isShiftDown ());
          break;

        case BACK_SPACE:
          cursor.backspace ();
          break;

        case DELETE:
          cursor.delete ();
          break;

        case END:
          cursor.eraseEOL ();
          break;

        case INSERT:
          screen.toggleInsertMode ();
          break;

        case HOME:
          cursor.home ();
          break;

        case ESCAPE:
          System.out.println ("escape");    // CLR key?
          break;

        default:
          boolean found = false;
          int pfKey = 0;
          for (KeyCode keyCode : AIDCommand.PFKeyCodes)
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
            sendAID (AIDCommand.PFKeyValues[pfKey]);
          }
          break;
      }
  }

  private void sendAID (byte aid)
  {
    screen.lockKeyboard ();
    screen.setAID (aid);

    AIDCommand command = screen.readModifiedFields ();
    consoleStage.sendAID (command);
  }
}