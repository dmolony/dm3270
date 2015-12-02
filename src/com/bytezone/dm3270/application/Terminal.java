package com.bytezone.dm3270.application;

import java.util.Arrays;
import java.util.Optional;
import java.util.prefs.Preferences;

import com.bytezone.dm3270.application.Console.Function;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.plugins.PluginsStage;
import com.bytezone.dm3270.utilities.WindowSaver;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Terminal extends Application
{
  private Preferences prefs;
  private ConsolePane consolePane;
  private WindowSaver windowSaver;
  private SiteListStage serverSitesListStage;
  private PluginsStage pluginsStage;
  private Screen screen;

  @Override
  public void init () throws Exception
  {
    super.init ();

    prefs = Preferences.userNodeForPackage (this.getClass ());
    for (String raw : getParameters ().getRaw ())
      if (raw.equalsIgnoreCase ("-reset"))
        prefs.clear ();

    if (true)
    {
      String[] keys = prefs.keys ();
      Arrays.sort (keys);
      for (String key : keys)
        if (key.matches ("Server.*Name"))
          System.out.printf ("%-18s : %s%n", key, prefs.get (key, ""));
    }
  }

  @Override
  public void start (Stage primaryStage) throws Exception
  {
    serverSitesListStage = new SiteListStage (prefs, "Server", 5, true);
    pluginsStage = new PluginsStage (prefs);

    Optional<Site> optionalServerSite = serverSitesListStage.getSelectedSite ();
    Site serverSite = optionalServerSite.get ();

    screen = new Screen (24, 80, prefs, Function.TERMINAL, pluginsStage, serverSite);

    consolePane = new ConsolePane (screen, serverSite, pluginsStage);
    consolePane.connect (serverSite);
    Scene scene = new Scene (consolePane);

    windowSaver = new WindowSaver (prefs, primaryStage, "Terminal");
    if (!windowSaver.restoreWindow ())
      primaryStage.centerOnScreen ();

    primaryStage.setScene (scene);
    primaryStage.setTitle ("dm3270");

    scene.setOnKeyPressed (new ConsoleKeyPress (consolePane, screen));
    scene.setOnKeyTyped (new ConsoleKeyEvent (screen));

    primaryStage.sizeToScene ();
    primaryStage.show ();
  }

  @Override
  public void stop ()
  {
    if (consolePane != null)
      consolePane.disconnect ();

    if (windowSaver != null)
      windowSaver.saveWindow ();

    if (screen != null)
      screen.closeAssistantStage ();
  }

  public static void main (String[] args)
  {
    launch (args);
  }
}