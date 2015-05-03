package com.bytezone.dm3270.plugins;

public interface Plugin
{
  public enum ProcessType
  {
    AUTO, REQUEST
  }

  public void activate ();

  public PluginReply processAuto (PluginScreen screen);

  public PluginReply processOnRequest (PluginScreen screen);

  public void deactivate ();

  public boolean doesAuto ();

  public boolean doesRequest ();
}