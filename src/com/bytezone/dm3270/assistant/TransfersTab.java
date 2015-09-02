package com.bytezone.dm3270.assistant;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.TSOCommandListener;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class TransfersTab extends AbstractTransferTab
    implements TSOCommandListener, ScreenChangeListener
{

  public TransfersTab (Screen screen, TextField text, Button execute)
  {
    super ("Transfers", screen, text, execute);
  }

  @Override
      void setText ()
  {
  }

  @Override
  public void screenChanged ()
  {
  }

  @Override
  public void tsoCommand (String command)
  {
  }

}