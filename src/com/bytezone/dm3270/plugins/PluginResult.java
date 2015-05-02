package com.bytezone.dm3270.plugins;

import java.util.ArrayList;
import java.util.List;

public class PluginResult
{
  private final List<ScreenField> fieldsChanged = new ArrayList<> ();
  private int key;
  private int cursorRow;
  private int cursorColumn;
  private boolean cursorMoved;
  private boolean keyPressed;
  private boolean showScreen;

  protected void add (ScreenField field)
  {
    fieldsChanged.add (field);
  }

  protected void setKey (int key)
  {
    this.key = key;
    keyPressed = true;
  }

  protected void setCursor (int row, int column)
  {
    cursorRow = row;
    cursorColumn = column;
    cursorMoved = true;
  }

  protected void setShowScreen (boolean showScreen)
  {
    this.showScreen = showScreen;
  }

  public boolean getShowScreen ()
  {
    return showScreen;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    if (keyPressed)
      text.append (String.format ("Key pressed.... %d%n", key));

    if (cursorMoved)
      text.append (String.format ("Cursor...... %d / %d%n", cursorRow, cursorColumn));

    if (fieldsChanged.size () > 0)
    {
      text.append ("Fields changed..\n");
      for (ScreenField field : fieldsChanged)
        text.append (String.format ("  Field..... %s%n", field.newData));
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}