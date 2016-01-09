package com.bytezone.dm3270.assistant;

import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenWatcher;

import javafx.scene.control.Tab;

public abstract class AbstractTransferTab extends Tab
    implements ScreenChangeListener, KeyboardStatusListener
{
  protected final Screen screen;
  protected ScreenWatcher screenWatcher;
  protected TSOCommand tsoCommand;

  public AbstractTransferTab (String name, Screen screen, TSOCommand tsoCommand)
  {
    super (name);

    setClosable (false);

    this.screen = screen;
    this.tsoCommand = tsoCommand;
  }

  protected void eraseCommand ()
  {
    tsoCommand.txtCommand.setText ("");
    tsoCommand.btnExecute.setDisable (true);
  }

  abstract protected void setText ();

  protected void setButton ()
  {
    tsoCommand.btnExecute.setDisable (screen.isKeyboardLocked () || screenWatcher == null
        || screenWatcher.getTSOCommandField () == null
        || tsoCommand.txtCommand.getText ().isEmpty ());
  }

  @Override
  public void screenChanged (ScreenWatcher screenDetails)
  {
    this.screenWatcher = screenDetails;
    if (isSelected () && screenDetails != null)
      setText ();
  }

  @Override
  public void keyboardStatusChanged (KeyboardStatusChangedEvent evt)
  {
    if (isSelected ())
      setButton ();
  }
}