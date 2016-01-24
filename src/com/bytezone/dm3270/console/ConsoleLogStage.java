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
  private final Tab consoleTab = new Tab ("Console");
  private final ConsoleMessageTab consoleMessageTab = new ConsoleMessageTab ();

  public ConsoleLogStage (Screen screen)
  {
    setTitle ("Console Logs");

    setOnCloseRequest (e -> closeWindow ());
    btnHide.setOnAction (e -> closeWindow ());

    BorderPane borderPane = new BorderPane ();
    borderPane.setTop (menuBar);
    borderPane.setCenter (tabPane);

    consoleTab.setClosable (false);

    menuBar.setUseSystemMenuBar (SYSTEM_MENUBAR);
    tabPane.getTabs ().addAll (consoleTab, consoleMessageTab);
    tabPane.setTabMinWidth (80);

    Scene scene = new Scene (borderPane, 800, 500);             // width/height
    setScene (scene);

    windowSaver = new WindowSaver (prefs, this, "DatasetStage");
    windowSaver.restoreWindow ();

    tabPane.getSelectionModel ().select (consoleTab);
  }

  public void setConsoleLog (ConsoleLog consoleLog)
  {
    consoleTab.setContent (consoleLog.getTextArea ());
    consoleLog.addConsoleMessageListener (consoleMessageTab);

    show ();
  }

  public void closeWindow ()
  {
    windowSaver.saveWindow ();
    hide ();
  }
}