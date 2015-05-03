package com.bytezone.dm3270.plugins;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.input.KeyCode;

public class PluginData
{
  public final int sequence;
  public final List<ScreenField> screenFields;
  public final int initialCursorRow;
  public final int initialCursorColumn;

  public int cursorRow;
  public int cursorColumn;
  public KeyCode key;
  public final List<ScreenField> changedFields = new ArrayList<> ();

  public PluginData (int sequence, int row, int column, List<ScreenField> fields)
  {
    this.sequence = sequence;
    initialCursorRow = row;
    initialCursorColumn = column;
    screenFields = fields;
  }

  public void addChangedField (ScreenField field)
  {
    changedFields.add (field);
  }

  public void setKey (KeyCode key)
  {
    this.key = key;
  }

  public void setNewCursorPosition (int row, int column)
  {
    cursorRow = row;
    cursorColumn = column;
  }

  public boolean cursorMoved ()
  {
    return initialCursorRow != cursorRow || initialCursorColumn != cursorColumn;
  }
}