package com.bytezone.dm3270.plugins;

import java.util.ArrayList;
import java.util.List;

public class PluginData
{
  public final int sequence;
  public final List<PluginField> screenFields;
  public final ScreenLocation initialCursorLocation;

  public ScreenLocation newCursorLocation;
  public byte key;
  public final List<PluginField> changedFields = new ArrayList<> ();
  public boolean suppressDisplay;

  public PluginData (int sequence, ScreenLocation screenLocation,
      List<PluginField> fields)
  {
    this.sequence = sequence;
    //    initialCursorLocation = new ScreenLocation (row, column);
    initialCursorLocation = screenLocation;
    screenFields = fields;
  }

  public void addChangedField (PluginField field)
  {
    if (!changedFields.contains (field))
      changedFields.add (field);
  }

  public String trimField (int index)
  {
    if (index >= 0 && index < screenFields.size ())
    {
      String data = screenFields.get (index).getFieldValue ();
      if (data != null)
        return data.trim ();
    }
    return "";
  }

  public void setKey (byte key)
  {
    this.key = key;
  }

  public byte getKey ()
  {
    return key;
  }

  public int size ()
  {
    return screenFields.size ();
  }

  public PluginField getField (int index)
  {
    if (index >= 0 && index < screenFields.size ())
    {
      PluginField field = screenFields.get (index);
      field.setData (this);
      return field;
    }
    return null;
  }

  public PluginField getField (ScreenLocation location)
  {
    for (PluginField field : screenFields)
      if (field.contains (location))
      {
        field.setData (this);
        return field;
      }

    return null;
  }

  public PluginField getField (String value)
  {
    for (PluginField field : screenFields)
      if (field.getFieldValue ().equals (value))
      {
        field.setData (this);
        return field;
      }

    return null;
  }

  public PluginField getCursorField ()
  {
    return getField (initialCursorLocation);
  }

  public String listFields ()
  {
    StringBuilder text = new StringBuilder ();
    int count = 0;
    for (PluginField field : screenFields)
      text.append (String.format ("%3d  %s%n", count++, field));
    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  public void setNewCursorPosition (int row, int column)
  {
    newCursorLocation = new ScreenLocation (row, column);
  }

  public int getNewCursorLocation ()
  {
    return newCursorLocation.location;
  }

  public List<PluginField> getModifiableFields ()
  {
    List<PluginField> fields = new ArrayList<> ();
    for (PluginField field : screenFields)
      if (!field.isProtected)
        fields.add (field);
    return fields;
  }

  public boolean cursorMoved ()
  {
    return newCursorLocation != null;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Sequence      : %d%n", sequence));
    text.append (String.format ("Screen fields : %d%n", screenFields.size ()));
    text.append (String.format ("Modifiable    : %d%n", getModifiableFields ().size ()));
    text.append (String.format ("Cursor field  : %s%n", getCursorField ()));

    int count = 0;
    for (PluginField sf : screenFields)
    {
      String fieldText =
          String.format ("%s%s", sf.isProtected ? "P" : "p", sf.isAlpha ? "A" : "a");
      text.append (String.format (" %3d : %s %s%n", count++, fieldText, sf));
    }

    return text.toString ();
  }
}