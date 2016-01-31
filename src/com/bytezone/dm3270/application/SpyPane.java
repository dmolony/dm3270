package com.bytezone.dm3270.application;

import java.io.File;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.session.Session;
import com.bytezone.dm3270.session.SessionTable;
import com.bytezone.dm3270.streams.SpyServer;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.utilities.Site;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

public class SpyPane extends BorderPane
{
  private static final int BUTTON_WIDTH = 105;

  private SpyServer spyServer;
  private Thread serverThread;
  private final TelnetState telnetState;

  public SpyPane (Screen screen, Site serverSite, Site clientSite,
      TelnetState telnetState)
  {
    this.telnetState = telnetState;

    SessionTable sessionTable = new SessionTable ();
    Session session = new Session (telnetState);

    spyServer = new SpyServer (serverSite, clientSite.getPort (), session, telnetState);
    spyServer.setScreen (screen);

    final Label label = session.getHeaderLabel ();
    label.setFont (new Font ("Arial", 20));
    label.setPadding (new Insets (10, 10, 10, 10));         // trbl

    CommandPane commandPane =
        new CommandPane (sessionTable, CommandPane.ProcessInstruction.DontProcess);

    Button btnSave = new Button ("Full Save");
    Button btnScramble = new Button ("Redacted");

    btnSave.setPrefWidth (BUTTON_WIDTH);
    btnScramble.setPrefWidth (BUTTON_WIDTH);

    final HBox hbox = new HBox ();
    hbox.setSpacing (15);
    hbox.setPadding (new Insets (10, 10, 10, 10));          // trbl
    hbox.getChildren ().addAll (btnSave, btnScramble);

    SplitPane splitPane = new SplitPane ();
    splitPane.setOrientation (Orientation.HORIZONTAL);

    splitPane.getItems ().addAll (sessionTable, commandPane);
    splitPane.setDividerPositions (0.37f);

    setCenter (splitPane);
    setTop (label);
    setBottom (hbox);

    String message = String
        .format ("Connect a terminal to localhost:%d%n%nWill connect to %s:%d",
                 clientSite.getPort (), serverSite.getURL (), serverSite.getPort ());

    sessionTable.setPlaceholder (new Label (message));
    sessionTable.setItems (session.getDataRecords ());

    btnSave.setOnAction ( (e) ->
    {
      FileChooser fileChooser = new FileChooser ();
      fileChooser.setTitle ("Save Session");
      File file = fileChooser.showSaveDialog (this.getScene ().getWindow ());
      if (file != null)
        session.save (file);
    });

    btnScramble.setOnAction ( (e) ->
    {
      FileChooser fileChooser = new FileChooser ();
      fileChooser.setTitle ("Save Session");
      File file = fileChooser.showSaveDialog (this.getScene ().getWindow ());
      if (file != null)
      {
        btnSave.setDisable (true);      // no going back
        session.safeSave (file);
      }
    });
  }

  protected VBox getVBox ()
  {
    VBox vbox = new VBox ();
    vbox.setSpacing (15);
    vbox.setPadding (new Insets (10, 10, 10, 10));    // trbl
    return vbox;
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