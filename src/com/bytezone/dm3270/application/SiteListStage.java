package com.bytezone.dm3270.application;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SiteListStage extends BasicStage
{
  Preferences prefs;
  List<Site> sites = new ArrayList<> ();

  public SiteListStage (Preferences prefs, String key, int max)
  {
    this.prefs = prefs;

    readPrefs (key, max);

    VBox vbox = getVBox ();
    for (Site site : sites)
    {
      HBox hbox = getHBox ();
      hbox.getChildren ().addAll (site.name, site.url, site.port);
      vbox.getChildren ().add (hbox);
    }

    Scene scene = new Scene (vbox);
    setScene (scene);
  }

  private void readPrefs (String key, int max)
  {
    int count = 0;
    while (count < max)
    {
      String keyName = String.format ("%s%02d", key, count++);

      String name = prefs.get (keyName + "Name", "");
      String url = prefs.get (keyName + "URL", "");
      int port = prefs.getInt (keyName + "Port", 23);

      if (port <= 0)
        port = 23;

      if (name.isEmpty () || url.isEmpty ())
        sites.add (new Site ("", "", 23));
      else
        sites.add (new Site (name, url, port));
    }
  }

  private void savePrefs (String key)
  {
    for (int i = 0; i < sites.size (); i++)
    {
      Site site = sites.get (i);
      String keyName = String.format ("%s%02d", key, i);
      prefs.put (keyName + "Name", site.name.getText ());
      prefs.put (keyName + "URL", site.url.getText ());
      prefs.put (keyName + "Port", site.port.getText ());
    }
  }

  Site getSelected ()
  {
    return null;
  }

  ObservableList<String> getSiteList ()
  {
    List<String> list = new ArrayList<> ();
    ObservableList<String> observableList = FXCollections.observableList (list);
    return observableList;
  }
}