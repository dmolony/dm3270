package com.bytezone.dm3270.application;

import java.io.File;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.session.Session;
import com.bytezone.dm3270.session.Session.SessionMode;
import com.bytezone.dm3270.session.SessionRecord;
import com.bytezone.dm3270.session.SessionTable;
import com.bytezone.dm3270.streams.SpyServer;

public class SpyStage extends BasicTelnetStage
{
  private static final int BUTTON_WIDTH = 105;

  private final SessionTable table = new SessionTable ();
  private SpyServer spyServer;

  final RadioButton btnFieldMode;
  final RadioButton btnExtendedFieldMode;
  final RadioButton btnCharacterMode;

  public SpyStage (ScreenHandler screenHandler, Screen screen, String serverURL,
      int serverPort, int clientPort, boolean prevent3270E)
  {
    Session session =
        new Session (screenHandler, screen, new TelnetState (), SessionMode.SPY);

    spyServer = new SpyServer (serverURL, serverPort, clientPort, session);
    spyServer.prevent3270E (prevent3270E);

    final TextArea textArea = getTextArea (600);

    Button btnSave = new Button ("Save");
    Button btnReadBuffer = new Button ("Read Buffer");
    Button btnReadModified = new Button ("Read Modified");

    btnSave.setPrefWidth (BUTTON_WIDTH);
    btnReadBuffer.setPrefWidth (BUTTON_WIDTH);
    btnReadModified.setPrefWidth (BUTTON_WIDTH);

    final HBox hbox = getHBox ();
    hbox.getChildren ().addAll (btnReadBuffer, btnReadModified);

    final ToggleGroup modeGroup = new ToggleGroup ();

    // these buttons should not be enabled until a mainframe has connected
    // also they should not be enabled if 3270E is being used
    btnFieldMode = getRadioButton ("Field Mode", hbox, modeGroup);
    btnExtendedFieldMode = getRadioButton ("Extended Field Mode", hbox, modeGroup);
    btnCharacterMode = getRadioButton ("Character Mode", hbox, modeGroup);
    btnFieldMode.setSelected (true);

    modeGroup.selectedToggleProperty ().addListener (new OnToggleHandler ());

    hbox.getChildren ().addAll (btnSave);

    SplitPane splitPane = new SplitPane ();
    splitPane.setOrientation (Orientation.HORIZONTAL);

    splitPane.getItems ().addAll (table, textArea);
    splitPane.setDividerPositions (0.35f);

    BorderPane borderPane = new BorderPane ();
    borderPane.setCenter (splitPane);
    borderPane.setBottom (hbox);

    Scene scene = new Scene (borderPane, 900, 800);     // width/height
    setTitle ("Terminal Spy");
    setScene (scene);
    setX (80);
    setY (30);

    String message =
        String.format ("Connect a terminal to localhost:%d%n%n"
            + "Will connect to mainframe at %s:%d", clientPort, serverURL, serverPort);

    table.setPlaceholder (new Label (message));
    table.setItems (spyServer.getSession ().getDataRecords ());

    table
        .getSelectionModel ()
        .selectedItemProperty ()
        .addListener ( (ObservableValue<? extends SessionRecord> observable,
                          SessionRecord oldValue, SessionRecord newValue) //
                      -> replay (newValue, textArea, null, DONT_PROCESS));

    btnReadBuffer.setOnAction ( (x) -> {
      spyServer.writeToClientSocket (createReadBufferCommand (Command.READ_BUFFER_F2));
    });

    btnReadModified.setOnAction ( (x) -> {
      spyServer.writeToClientSocket (createReadBufferCommand (Command.READ_MODIFIED_F6));
    });

    btnSave.setOnAction ( (e) -> {
      FileChooser fileChooser = new FileChooser ();
      fileChooser.setTitle ("Save Session");
      File file = fileChooser.showSaveDialog (this);
      if (file != null)
        spyServer.getSession ().save (file);
    });
  }

  private class OnToggleHandler implements ChangeListener<Toggle>
  {
    @Override
    public void changed (ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1)
    {
      try
      {
        if (t1 == btnFieldMode)
          spyServer.writeToClientSocket (createSetReplyModeCommand ((byte) 0x00));
        else if (t1 == btnExtendedFieldMode)
          spyServer.writeToClientSocket (createSetReplyModeCommand ((byte) 0x01));
        else if (t1 == btnCharacterMode)
          spyServer.writeToClientSocket (createSetReplyModeCommand ((byte) 0x02));
      }
      catch (Exception e)
      {
        e.printStackTrace ();
      }
    }
  }

  public void startServer ()
  {
    new Thread (spyServer).start ();
  }

  public void disconnect ()
  {
    if (spyServer != null)
    {
      spyServer.close ();
      spyServer = null;
    }
  }
}