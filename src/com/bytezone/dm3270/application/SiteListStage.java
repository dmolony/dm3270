package com.bytezone.dm3270.application;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class SiteListStage extends BasicStage
{
  Preferences prefs;
  List<Site> sites = new ArrayList<> ();
  String key;

  public SiteListStage (Preferences prefs, String key)
  {
    this.prefs = prefs;
    this.key = key;

    readPrefs ();
  }

  private void readPrefs ()
  {
    int count = 0;
    while (true)
    {
      String keyName = String.format ("%s%02d", key, count);

      String name = prefs.get (keyName + "Name", "");
      String url = prefs.get (keyName + "URL", "");
      int port = prefs.getInt (keyName + "Port", 23);

      if (name.isEmpty () || url.isEmpty ())
        break;
      if (port <= 0)
        port = 23;

      sites.add (new Site (name, url, port));
    }
  }

  private void savePrefs ()
  {
    for (int i = 0; i < sites.size (); i++)
    {
      Site site = sites.get (i);
      String keyName = String.format ("%s%02d", key, i);
      prefs.put (keyName + "Name", site.name);
      prefs.put (keyName + "URL", site.url);
      prefs.putInt (keyName + "Port", site.port);
    }
  }

  private class Site
  {
    String name;
    String url;
    int port;

    public Site (String name, String url, int port)
    {
      this.name = name;
      this.url = url;
      this.port = port;
    }
  }
}