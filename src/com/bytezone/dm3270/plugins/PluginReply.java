package com.bytezone.dm3270.plugins;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.input.KeyCode;

class PluginReply
{
  private final List<ScreenField> fieldsChanged = new ArrayList<> ();
  private KeyCode key;
  private int cursorRow;
  private int cursorColumn;
  private boolean cursorMoved;
  private boolean showScreen;

  public void add (ScreenField field)
  {
    fieldsChanged.add (field);
  }

  public List<ScreenField> getScreenFields ()
  {
    return fieldsChanged;
  }

  public void setKey (KeyCode key)
  {
    this.key = key;
  }

  public KeyCode getKey ()
  {
    return key;
  }

  public void setCursor (int row, int column)
  {
    cursorRow = row;
    cursorColumn = column;
    cursorMoved = true;
  }

  public void setShowScreen (boolean showScreen)
  {
    this.showScreen = showScreen;
  }

  public boolean getShowScreen ()
  {
    return showScreen;
  }

  public boolean cursorMoved ()
  {
    return cursorMoved;
  }

  public int getCursorRow ()
  {
    return cursorRow;
  }

  public int getCursorColumn ()
  {
    return cursorColumn;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Key pressed .... %s%n", key == null ? "None" : key));
    text.append (String.format ("Cursor ......... %s%n",
                                cursorMoved ? String.format ("%d / %d", cursorRow,
                                                             cursorColumn) : "None"));
    text.append (String.format ("Show screen .... %s%n", showScreen));

    if (fieldsChanged.size () > 0)
    {
      text.append ("Fields changed:\n");
      for (ScreenField field : fieldsChanged)
        text.append (String.format ("    %02d / %02d .... %s%n", field.row, field.column,
                                    field.newData));
    }

    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}