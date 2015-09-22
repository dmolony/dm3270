package com.bytezone.dm3270.assistant;

import com.bytezone.dm3270.application.KeyboardStatusListener;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenChangeListener;

import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;

public abstract class AbstractTransferTab extends Tab
    implements ScreenChangeListener, KeyboardStatusListener
{
  protected final Screen screen;
  protected final Button btnExecute;
  protected final TextField txtCommand;

  public AbstractTransferTab (String name, Screen screen, TextField text, Button execute)
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

  abstract protected void setText ();
}