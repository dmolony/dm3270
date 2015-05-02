package com.bytezone.dm3270.plugins;

import java.util.ArrayList;
import java.util.List;

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

  protected int countModifiableFields (PluginScreen screen)
  {
    int count = 0;
    for (ScreenField field : screen.screenFields)
      if (!field.isProtected)
        ++count;
    return count;
  }

  protected List<ScreenField> getModifiableFields (PluginScreen screen)
  {
    List<ScreenField> fields = new ArrayList<> ();
    for (ScreenField field : screen.screenFields)
      if (!field.isProtected)
        fields.add (field);
    return fields;
  }

  protected List<ScreenField> getProtectedFields (PluginScreen screen)
  {
    List<ScreenField> fields = new ArrayList<> ();
    for (ScreenField field : screen.screenFields)
      if (field.isProtected)
        fields.add (field);
    return fields;
  }

  protected List<ScreenField> getAlphanumericFields (PluginScreen screen)
  {
    List<ScreenField> fields = new ArrayList<> ();
    for (ScreenField field : screen.screenFields)
      if (field.isAlpha)
        fields.add (field);
    return fields;
  }

  protected List<ScreenField> getNumericFields (PluginScreen screen)
  {
    List<ScreenField> fields = new ArrayList<> ();
    for (ScreenField field : screen.screenFields)
      if (!field.isAlpha)
        fields.add (field);
    return fields;
  }
}