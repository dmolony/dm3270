package com.bytezone.dm3270.plugins;

public interface Plugin
{
  default void activate ()
  {

  }

  default void deactivate ()
  {

  }

  default boolean doesAuto ()
  {
    return false;
  }

  default boolean doesRequest ()
  {
    return false;
  }

  default PluginReply processAuto (PluginScreen screen)
  {
    return null;
  }

  default PluginReply processOnRequest (PluginScreen screen)
  {
    return null;
  }
}