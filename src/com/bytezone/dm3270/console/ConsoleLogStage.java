package com.bytezone.dm3270.console;

import java.util.prefs.Preferences;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.utilities.WindowSaver;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
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
  private final Tab console1Tab = new Tab ("IPL");
  private final Tab console2Tab = new Tab ("Console");
  private final ConsoleMessageTab consoleMessageTab = new ConsoleMessageTab ();

  public ConsoleLogStage (Screen screen)
  {
    setTitle ("Console Logs");

    setOnCloseRequest (e -> closeWindow ());
    btnHide.setOnAction (e -> closeWindow ());

    BorderPane borderPane = new BorderPane ();
    borderPane.setTop (menuBar);
    borderPane.setCenter (tabPane);

    console1Tab.setClosable (false);
    console2Tab.setClosable (false);

    menuBar.setUseSystemMenuBar (SYSTEM_MENUBAR);
    tabPane.getTabs ().addAll (console1Tab, console2Tab, consoleMessageTab);
    tabPane.setTabMinWidth (80);

    Scene scene = new Scene (borderPane, 800, 500);             // width/height
    setScene (scene);

    windowSaver = new WindowSaver (prefs, this, "DatasetStage");
    windowSaver.restoreWindow ();

    tabPane.getSelectionModel ().select (console1Tab);
  }

  public void setLogs (ConsoleLog1 consoleLog1, ConsoleLog2 consoleLog2)
  {
    console1Tab.setContent (consoleLog1.getTextArea ());
    console2Tab.setContent (consoleLog2.getTextArea ());

    consoleLog2.addConsoleMessageListener (consoleMessageTab);

    show ();
  }

  public void closeWindow ()
  {
    windowSaver.saveWindow ();
    hide ();
  }
}