package com.bytezone.dm3270.plugins;

public abstract class DefaultPlugin implements Plugin
{
  @Override
  public void activate ()
  {
  }

  @Override
  public void deactivate ()
  {
  }

  @Override
  public boolean doesAuto ()
  {
    return true;
  }

  @Override
  public boolean doesRequest ()
  {
    return false;
  }

  @Override
  public PluginResult processOnRequest (PluginScreen screen)
  {
    return null;
  }

  protected long getMD5 ()
  {
    return 0;
  }
}