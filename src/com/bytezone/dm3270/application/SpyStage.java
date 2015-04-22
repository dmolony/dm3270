package com.bytezone.dm3270.application;

import java.io.File;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.session.Session;
import com.bytezone.dm3270.session.SessionTable;
import com.bytezone.dm3270.streams.SpyServer;
import com.bytezone.dm3270.streams.TelnetState;

public class SpyStage extends BasicTelnetStage
{
  private static final int BUTTON_WIDTH = 105;

  private SpyServer spyServer;
  private Thread serverThread;

  public SpyStage (Screen screen, Site serverSite, Site clientSite)
  {
    SessionTable table = new SessionTable ();
    Session session = new Session (screen, new TelnetState ());

    spyServer = new SpyServer (serverSite, clientSite.getPort (), session);

    final Label label = session.getHeaderLabel ();
    label.setFont (new Font ("Arial", 20));
    label.setPadding (new Insets (10, 10, 10, 10));    // trbl

    CommandPane commandPane =
        new CommandPane (screen, table, ProcessInstruction.DontProcess);

    Button btnSave = new Button ("Full Save");
    Button btnScramble = new Button ("Redacted");

    btnSave.setPrefWidth (BUTTON_WIDTH);
    btnScramble.setPrefWidth (BUTTON_WIDTH);

    final HBox hbox = new HBox ();
    hbox.setSpacing (15);
    hbox.getChildren ().addAll (btnSave, btnScramble);

    final VBox leftPane = getVBox ();
    leftPane.getChildren ().addAll (table, hbox);

    SplitPane splitPane = new SplitPane ();
    splitPane.setOrientation (Orientation.HORIZONTAL);

    splitPane.getItems ().addAll (leftPane, commandPane);
    splitPane.setDividerPositions (0.37f);

    BorderPane borderPane = new BorderPane ();
    borderPane.setCenter (splitPane);
    borderPane.setTop (label);

    setTitle ("Terminal Spy");

    Scene scene = new Scene (borderPane);
    setScene (scene);

    String message =
        String.format ("Connect a terminal to localhost:%d%n%n"
                           + "Will connect to mainframe at %s:%d", clientSite.getPort (),
                       serverSite.getURL (), serverSite.getPort ());

    table.setPlaceholder (new Label (message));
    table.setItems (session.getDataRecords ());

    //    Rectangle2D primaryScreenBounds =
    //        javafx.stage.Screen.getPrimary ().getVisualBounds ();
    //    String osName = System.getProperty ("os.name");
    //    if (osName.startsWith ("Mac"))
    //    {
    //      System.out.println (primaryScreenBounds.getMinX ()
    //          + primaryScreenBounds.getWidth ());
    //      System.out.println (primaryScreenBounds.getMinY ());
    //    }
    setX (1200);
    setY (20);

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

    setOnCloseRequest (e -> Platform.exit ());
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