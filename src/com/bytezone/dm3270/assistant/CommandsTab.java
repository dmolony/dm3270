package com.bytezone.dm3270.assistant;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenDetails;
import com.bytezone.dm3270.display.TSOCommandListener;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class CommandsTab extends TransferTab
    implements TSOCommandListener, ScreenChangeListener
{
  List<String> commands = new ArrayList<> ();

  public CommandsTab (Screen screen, TextField text, Button execute)
  {
    super ("Commands", screen, text, execute);
  }

  @Override
      void setText ()
  {
    ScreenDetails screenDetails = screen.getScreenDetails ();
    if (screenDetails.getTSOCommandField () == null)
    {
      eraseCommand ();
      return;
    }

    System.out.printf ("checking: %slocked%n",
                       screenDetails.isKeyboardLocked () ? "" : "un");
    if (screenDetails.isKeyboardLocked ())
    {
      btnExecute.setDisable (true);
      return;
    }

    if (commands.size () > 0)
    {
      txtCommand.setText (commands.get (commands.size () - 1));
      btnExecute.setDisable (false);
      System.out.println ("click me");
    }
  }

  @Override
  public void tsoCommand (String command)
  {
    commands.add (command);
    if (isSelected ())
      setText ();
  }

  @Override
  public void screenChanged ()
  {
    if (isSelected ())
      setText ();
  }
}