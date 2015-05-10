package com.bytezone.dm3270.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parameters
{
  //  String fileName = "dm3270/prefs.txt";
  List<SiteParameters> siteParameters = new ArrayList<> ();

  public Parameters ()
  {
    SiteParameters currentSiteParameters = null;

    List<String> lines = getLines ();
    for (String line : lines)
    {
      if (line.startsWith ("["))
      {
        currentSiteParameters = new SiteParameters (line);
        siteParameters.add (currentSiteParameters);
      }
      else if (line.length () > 0)
        currentSiteParameters.addParameter (line);
    }
  }

  private List<String> getLines ()
  {
    String home = System.getProperty ("user.home");
    try
    {
      Path path = Paths.get (home, "dm3270", "prefs.txt");
      List<String> lines = Files.readAllLines (path);
      return lines;
    }
    catch (IOException e)
    {
      e.printStackTrace ();
      return new ArrayList<String> ();
    }
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    for (SiteParameters sp : siteParameters)
      text.append (sp + "\n");

    return text.toString ();
  }

  class SiteParameters
  {
    String siteName;
    Map<String, String> parameters = new HashMap<> ();

    public SiteParameters (String line)
    {
      siteName = line.substring (1, line.length () - 2);
    }

    public void addParameter (String line)
    {
      String[] data = line.split ("=");
      assert data.length == 2;
      parameters.put (data[0], data[1]);
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Site......... %s%n", siteName));
      for (String key : parameters.keySet ())
        text.append (String.format ("%-10s %s%n", key, parameters.get (key)));

      return text.toString ();
    }
  }
}