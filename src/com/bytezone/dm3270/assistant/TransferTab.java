package com.bytezone.dm3270.assistant;

import com.bytezone.dm3270.display.Screen;

import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;

public abstract class TransferTab extends Tab
{
  protected final Screen screen;
  protected final Button btnExecute;
  protected final TextField txtCommand;

  public TransferTab (String name, Screen screen, TextField text, Button execute)
  {
    super (name);

    setClosable (false);
    this.screen = screen;
    this.txtCommand = text;
    this.btnExecute = execute;
  }

  protected void eraseCommand ()
  {
    txtCommand.setText ("");
    btnExecute.setDisable (true);
  }

  abstract void setText ();
}