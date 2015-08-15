package com.bytezone.dm3270.assistant;

import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;

public abstract class TransferTab extends Tab
{
  protected final Button btnExecute;
  protected final TextField txtCommand;

  public TransferTab (String name, TextField text, Button execute)
  {
    super (name);

    setClosable (false);
    this.txtCommand = text;
    this.btnExecute = execute;
  }

  abstract void setText ();

  abstract void setButton ();
}