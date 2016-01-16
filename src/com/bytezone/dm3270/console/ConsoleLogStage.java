package com.bytezone.dm3270.console;

import java.util.prefs.Preferences;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.utilities.WindowSaver;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ConsoleLogStage extends Stage
{
  private final static String OS = System.getProperty ("os.name");
  private final static boolean SYSTEM_MENUBAR = OS != null && OS.startsWith ("Mac");

  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private final Button btnHide = new Button ("Hide Window");
  private final WindowSaver windowSaver;
  private final MenuBar menuBar = new MenuBar ();

  private final TabPane tabPane = new TabPane ();
  private final Console1Tab console1Tab = new Console1Tab ("IPL");
  private final Console2Tab console2Tab = new Console2Tab ("Console");

  public ConsoleLogStage (Screen screen)
  {
    setTitle ("Console Logs");

    setOnCloseRequest (e -> closeWindow ());
    btnHide.setOnAction (e -> closeWindow ());

    BorderPane borderPane = new BorderPane ();
    borderPane.setTop (menuBar);
    borderPane.setCenter (tabPane);

    menuBar.setUseSystemMenuBar (SYSTEM_MENUBAR);
    tabPane.getTabs ().addAll (console1Tab, console2Tab);
    tabPane.setTabMinWidth (80);

    Scene scene = new Scene (borderPane, 800, 500);             // width/height
    setScene (scene);

    windowSaver = new WindowSaver (prefs, this, "DatasetStage");
    windowSaver.restoreWindow ();

    tabPane.getSelectionModel ().select (console1Tab);
  }

  public void closeWindow ()
  {
    windowSaver.saveWindow ();
    hide ();
  }
}