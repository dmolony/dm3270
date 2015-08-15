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
import javafx.scene.text.Font;

public class TSOCommand implements TSOCommandStatusListener
{
  private final HBox hbox = new HBox (10);

  private final Label lblCommand = new Label ("TSO Command");
  final TextField txtCommand = new TextField ();
  final Button btnExecute = new Button ("Execute");

  private ConsolePane consolePane;
  private ScreenDetails screenDetails;

  public TSOCommand ()
  {
    txtCommand.setEditable (false);
    txtCommand.setPrefWidth (320);
    txtCommand.setFont (Font.font ("Monospaced", 12));
    txtCommand.setFocusTraversable (false);

    btnExecute.setOnAction (e -> execute ());

    hbox.getChildren ().addAll (lblCommand, txtCommand, btnExecute);
    hbox.setAlignment (Pos.CENTER_LEFT);
  }

  public HBox getBox ()
  {
    return hbox;
  }

  public void setConsolePane (ConsolePane consolePane)
  {
    this.consolePane = consolePane;
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