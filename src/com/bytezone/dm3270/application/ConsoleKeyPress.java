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
    //    e.consume ();

    KeyCode keyCodePressed = e.getCode ();

    //    if (e.isControlDown () && keyCodePressed == KeyCode.S)
    //    {
    //      consolePane.toggleHistory ();
    //      return;
    //    }

    if (screen.isKeyboardLocked ())
    {
      if (e.isControlDown ())       // should allow user to choose modifier key
        if (keyCodePressed == KeyCode.LEFT)
        {
          consolePane.back ();
          e.consume ();
        }
        else if (keyCodePressed == KeyCode.RIGHT)
        {
          consolePane.forward ();
          e.consume ();
        }

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
          sendAID (AIDCommand.AID_PA1);
          e.consume ();
          break;

        case F2:
          sendAID (AIDCommand.AID_PA2);
          e.consume ();
          break;

        case F3:
          sendAID (AIDCommand.AID_PA3);
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
          sendAID (AIDCommand.AID_ENTER_KEY);
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
            e.consume ();
          }
          break;
      }
  }

  private void sendAID (byte aid)
  {
    screen.lockKeyboard ();
    screen.setAID (aid);

    AIDCommand command = screen.readModifiedFields ();
    consolePane.sendAID (command);
  }
}