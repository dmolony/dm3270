package com.bytezone.dm3270.assistant;

import java.util.prefs.Preferences;

import com.bytezone.dm3270.application.WindowSaver;
import com.bytezone.dm3270.display.TSOCommandStatusListener;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AssistantStage extends Stage
{
  private final static String OS = System.getProperty ("os.name");
  private final static boolean SYSTEM_MENUBAR = OS != null && OS.startsWith ("Mac");

  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private final WindowSaver windowSaver;
  private final MenuBar menuBar = new MenuBar ();

  private final TSOCommand tsoCommand = new TSOCommand ();
  private final Button btnHide = new Button ("Hide Window");
  private final TabPane tabPane = new TabPane ();

  public AssistantStage ()
  {
    setTitle ("Assistant");

    setOnCloseRequest (e -> closeWindow ());
    btnHide.setOnAction (e -> closeWindow ());

    AnchorPane anchorPane = new AnchorPane ();
    AnchorPane.setLeftAnchor (tsoCommand.getBox (), 10.0);
    AnchorPane.setBottomAnchor (tsoCommand.getBox (), 10.0);
    AnchorPane.setBottomAnchor (btnHide, 10.0);
    AnchorPane.setRightAnchor (btnHide, 10.0);
    anchorPane.getChildren ().addAll (tsoCommand.getBox (), btnHide);

    BorderPane borderPane = new BorderPane ();
    borderPane.setTop (menuBar);
    borderPane.setCenter (tabPane);
    borderPane.setBottom (anchorPane);

    if (SYSTEM_MENUBAR)
      menuBar.useSystemMenuBarProperty ().set (true);

    Scene scene = new Scene (borderPane, 800, 500);// width/height
    setScene (scene);

    windowSaver = new WindowSaver (prefs, this, "DatasetStage");
    windowSaver.restoreWindow ();
  }

  public TSOCommandStatusListener getTSOCommand ()
  {
    return tsoCommand;
  }

  private void closeWindow ()
  {
    windowSaver.saveWindow ();
    hide ();
  }
}