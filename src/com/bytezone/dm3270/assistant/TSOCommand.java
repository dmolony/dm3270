package com.bytezone.dm3270.assistant;

import java.io.File;

import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenWatcher;
import com.bytezone.dm3270.filetransfer.IndFileCommand;
import com.bytezone.dm3270.filetransfer.TransferManager;
import com.bytezone.dm3270.utilities.Dm3270Utility;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

public class TSOCommand implements ScreenChangeListener
{
  private final HBox hbox = new HBox (10);

  private final Label lblCommand = new Label ("TSO Command");
  final TextField txtCommand = new TextField ();
  final Button btnExecute = new Button ("Execute");

  private ConsolePane consolePane;
  private ScreenWatcher screenWatcher;
  private TransferManager transferManager;
  private byte[] buffer;
  private File file;

  public TSOCommand ()
  {
    txtCommand.setEditable (true);
    txtCommand.setPrefWidth (500);
    txtCommand.setFont (Font.font ("Monospaced", 13));
    txtCommand.setFocusTraversable (false);

    btnExecute.setOnAction (e -> execute ());

    hbox.getChildren ().addAll (lblCommand, txtCommand, btnExecute);
    hbox.setAlignment (Pos.CENTER_LEFT);
  }

  void setTransferManager (TransferManager transferManager)
  {
    this.transferManager = transferManager;
  }

  HBox getBox ()
  {
    return hbox;
  }

  void setBuffer (byte[] buffer, File file)
  {
    this.buffer = buffer;
    this.file = file;
  }

  public void setConsolePane (ConsolePane consolePane)
  {
    this.consolePane = consolePane;
  }

  private void execute ()
  {
    if (screenWatcher == null || consolePane == null)
      return;

    Field tsoCommandField = screenWatcher.getTSOCommandField ();
    if (tsoCommandField == null)
    {
      Dm3270Utility.showAlert ("This screen has no TSO input field");
      return;
    }

    String command = txtCommand.getText ();
    if (command.length () > tsoCommandField.getDisplayLength ())
    {
      Dm3270Utility.showAlert ("Command is too long for the TSO input field");
      System.out.printf ("Field: %d, command: %d%n", tsoCommandField.getDisplayLength (),
                         command.length ());
      return;
    }

    if (!command.isEmpty ())
    {
      // TSO OUT DMOLONYB(JOB62856) PRINT(JOB62856)
      // TSO IND$FILE GET JOB62856.OUTLIST ASCII CRLF
      // TSO IND$FILE GET JCL.CNTL(JOBCARD) ASCII CRLF

      if (TransferManager.isIndfileCommand (command))
      {
        try
        {
          IndFileCommand indFileCommand = new IndFileCommand (command);
          if (indFileCommand.isUpload ())
          {
            indFileCommand.setBuffer (buffer);
            indFileCommand.setLocalFile (file);
          }
          transferManager.prepareTransfer (indFileCommand);
        }
        catch (IllegalArgumentException e)
        {
          System.out.println (e);
        }
      }

      // this should be a single call to a command handler
      tsoCommandField.setText (command);
      consolePane.sendAID (AIDCommand.AID_ENTER, "ENTR");
    }
  }

  @Override
  public void screenChanged (ScreenWatcher screenDetails)
  {
    this.screenWatcher = screenDetails;
  }
}