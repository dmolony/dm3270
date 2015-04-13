package com.bytezone.dm3270.application;

import java.io.File;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.session.Session;
import com.bytezone.dm3270.session.SessionRecord;
import com.bytezone.dm3270.session.SessionTable;
import com.bytezone.dm3270.streams.SpyServer;
import com.bytezone.dm3270.streams.TelnetState;

public class SpyStage extends BasicTelnetStage
{
  private static final int BUTTON_WIDTH = 105;

  private final SessionTable table = new SessionTable ();
  private SpyServer spyServer;
  private final Session session;
  private Thread serverThread;

  public SpyStage (Screen screen, Site serverSite, Site clientSite, boolean prevent3270E)
  {
    session = new Session (screen, new TelnetState ());

    spyServer = new SpyServer (serverSite, clientSite.getPort (), session);
    spyServer.prevent3270E (prevent3270E);

    final TextArea textArea = getTextArea (600);

    Button btnSave = new Button ("Full Save");
    Button btnScramble = new Button ("Redacted");

    btnSave.setPrefWidth (BUTTON_WIDTH);
    btnScramble.setPrefWidth (BUTTON_WIDTH);

    final HBox hbox = getHBox ();
    hbox.getChildren ().addAll (btnSave, btnScramble);

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
                           + "Will connect to mainframe at %s:%d", clientSite.getPort (),
                       serverSite.getURL (), serverSite.getPort ());

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

    btnScramble.setOnAction ( (e) -> {
      FileChooser fileChooser = new FileChooser ();
      fileChooser.setTitle ("Save Session");
      File file = fileChooser.showSaveDialog (this);
      if (file != null)
      {
        btnSave.setDisable (true);      // no going back
        session.safeSave (file);
      }
    });

    setOnCloseRequest (new EventHandler<WindowEvent> ()
    {
      @Override
      public void handle (WindowEvent we)
      {
        Platform.exit ();
      }
    });
  }

  public void startServer ()
  {
    serverThread = new Thread (spyServer);
    serverThread.start ();
  }

  public void disconnect ()
  {
    if (spyServer != null)
    {
      spyServer.close ();
      spyServer = null;
    }

    if (serverThread != null)
      try
      {
        serverThread.join ();
      }
      catch (InterruptedException e)
      {
        e.printStackTrace ();
      }
  }
}