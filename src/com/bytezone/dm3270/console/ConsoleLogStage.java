package com.bytezone.dm3270.console;

import java.util.prefs.Preferences;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.utilities.WindowSaver;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
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

  private ConsoleLog1 consoleLog1;
  private ConsoleLog2 consoleLog2;

  private final Font displayFont = Font.font ("Monospaced", 13);

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

  public void setLogs (ConsoleLog1 consoleLog1, ConsoleLog2 consoleLog2)
  {
    this.consoleLog1 = consoleLog1;
    this.consoleLog2 = consoleLog2;

    console1Tab.setContent (consoleLog1.getTextArea ());
    consoleLog1.getTextArea ().setFont (displayFont);

    console2Tab.setContent (consoleLog2.getTextArea ());
    consoleLog2.getTextArea ().setFont (displayFont);

    show ();
  }

  public void closeWindow ()
  {
    windowSaver.saveWindow ();
    hide ();
  }
}