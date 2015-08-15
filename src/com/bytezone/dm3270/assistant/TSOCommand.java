package com.bytezone.dm3270.assistant;

import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.ScreenDetails;
import com.bytezone.dm3270.display.TSOCommandStatusListener;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class TSOCommand implements TSOCommandStatusListener
{
  private final HBox hbox = new HBox (10);

  private final Label lblCommand = new Label ("TSO Command");
  private final TextField txtCommand = new TextField ();

  private ConsolePane consolePane;
  private ScreenDetails screenDetails;

  private final Button btnExecute = new Button ("Execute");

  public TSOCommand ()
  {
    btnExecute.setOnAction (e -> execute ());

    hbox.getChildren ().addAll (lblCommand, txtCommand);
    hbox.setAlignment (Pos.CENTER_LEFT);
  }

  public HBox getBox ()
  {
    return hbox;
  }

  private void execute ()
  {
    if (screenDetails == null || consolePane == null)
      return;

    Field tsoCommandField = screenDetails.getTSOCommandField ();
    String command = txtCommand.getText ();

    if (tsoCommandField != null && !command.isEmpty ())
    {
      tsoCommandField.setText (command);
      consolePane.sendAID (AIDCommand.AID_ENTER, "ENTR");
    }
  }

  @Override
  public void screenChanged (ScreenDetails screenDetails)
  {
    this.screenDetails = screenDetails;
  }
}