package com.bytezone.dm3270.application;

import java.io.File;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.session.Session;
import com.bytezone.dm3270.session.Session.SessionMode;
import com.bytezone.dm3270.session.SessionRecord;
import com.bytezone.dm3270.session.SessionTable;
import com.bytezone.dm3270.streams.SpyServer;
import com.bytezone.dm3270.telnet.TelnetState;

public class SpyStage extends BasicTelnetStage
{
  private static final int BUTTON_WIDTH = 105;

  private final SessionTable table = new SessionTable ();
  private SpyServer spyServer;
  private final Session session;

  public SpyStage (Screen screen, String serverURL, int serverPort, int clientPort,
      boolean prevent3270E)
  {
    session = new Session (screen, new TelnetState (), SessionMode.SPY);

    spyServer = new SpyServer (serverURL, serverPort, clientPort, session);
    spyServer.prevent3270E (prevent3270E);

    final TextArea textArea = getTextArea (600);

    Button btnSave = new Button ("Save");

    btnSave.setPrefWidth (BUTTON_WIDTH);

    final HBox hbox = getHBox ();
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
    table.setItems (session.getDataRecords ());

    table
        .getSelectionModel ()
        .selectedItemProperty ()
        .addListener ( (ObservableValue<? extends SessionRecord> observable,
                          SessionRecord oldValue, SessionRecord newValue) //
                      -> replay (newValue, textArea, null, null, null, null, null,
                                 DONT_PROCESS, screen));

    btnSave.setOnAction ( (e) -> {
      FileChooser fileChooser = new FileChooser ();
      fileChooser.setTitle ("Save Session");
      File file = fileChooser.showSaveDialog (this);
      if (file != null)
        session.save (file);
    });
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