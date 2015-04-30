package com.bytezone.dm3270.plugins;

public interface Plugin
{
  public void activate ();

  public PluginResult process (PluginScreen screen);

  public void deactivate ();
}