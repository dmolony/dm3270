package com.bytezone.dm3270.display;

public interface TSOCommandStatusListener
{
  public abstract void screenChanged (boolean isTSOCommandScreen, Field tsoCommandField);
}