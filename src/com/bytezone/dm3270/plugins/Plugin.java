package com.bytezone.dm3270.plugins;

public interface Plugin
{
  public enum ProcessType
  {
    AUTO, REQUEST
  }

  public void activate ();

  public PluginResult processAuto (PluginScreen screen);

  public PluginResult processRequest (PluginScreen screen);

  public void deactivate ();

  boolean doesAuto ();

  boolean doesRequest ();
}