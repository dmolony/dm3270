package com.bytezone.dm3270.assistant;

import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenDetails;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

public class TSOCommand
{
  private final HBox hbox = new HBox (10);
  private final Screen screen;

  private final Label lblCommand = new Label ("TSO Command");
  final TextField txtCommand = new TextField ();
  final Button btnExecute = new Button ("Execute");

  private ConsolePane consolePane;

  public TSOCommand (Screen screen)
  {
    this.screen = screen;

    txtCommand.setEditable (true);
    txtCommand.setPrefWidth (400);
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
    ScreenDetails screenDetails = screen.getScreenDetails ();
    if (screenDetails == null || consolePane == null)
      return;

    Field tsoCommandField = screenDetails.getTSOCommandField ();
    String command = txtCommand.getText ();

    if (command.length () > tsoCommandField.getDisplayLength ())
    {
      System.out.println ("Command is too long for the input field");
      System.out.printf ("Field: %d, command: %d%n", tsoCommandField.getDisplayLength (),
                         command.length ());
      return;
    }

    if (tsoCommandField != null && !command.isEmpty ())
    {
      tsoCommandField.setText (command);
      consolePane.sendAID (AIDCommand.AID_ENTER, "ENTR");
    }
  }
}