package com.bytezone.dm3270.plugins;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PluginField
{
  private PluginData pluginData;

  public final ScreenLocation location;
  public final int sequence;
  public final boolean isProtected;
  public final boolean isModifiable;
  public final boolean isAlpha;
  public final boolean isVisible;
  public final boolean isAltered;

  private StringProperty fieldValue;
  private IntegerProperty sequenceValue;
  private IntegerProperty row;
  private IntegerProperty column;
  private IntegerProperty lengthValue;
  private StringProperty modifiable;
  private StringProperty visible;
  private StringProperty altered;
  private StringProperty format;

  public String newData;

  public PluginField (int sequence, ScreenLocation screenLocation, int length,
      boolean isProtected, boolean isAlpha, boolean isVisible, boolean isAltered,
      String data)
  {
    //    this.location = new ScreenLocation (location);
    this.location = screenLocation;
    this.sequence = sequence;
    this.isProtected = isProtected;
    this.isModifiable = !isProtected;
    this.isAlpha = isAlpha;
    this.isVisible = isVisible;
    this.isAltered = isAltered;

    setFieldValue (data);
    setSequence (sequence);
    setRow (this.location.row);
    setColumn (this.location.column);
    setLength (length);
    setModifiable (!isProtected ? "yes" : "no");
    setVisible (isVisible ? "yes" : "no");
    setAltered (isAltered ? "yes" : "no");
    setFormat (isAlpha ? "A" : "A/N");
  }

  public boolean isModifiableLength (int length)
  {
    return !isProtected && this.getLength () == length;
  }

  public void change (String newValue, PluginData data)
  {
    newData = newValue;
    data.addChangedField (this);
  }

  public void change (String newValue)
  {
    assert pluginData != null;
    change (newValue, pluginData);
  }

  public void setData (PluginData pluginData)
  {
    this.pluginData = pluginData;
  }

  public ScreenLocation getLocation ()
  {
    return location;
  }

  public boolean contains (ScreenLocation position)
  {
    if (location.location == position.location)
      return true;

    int first = location.location - 1;      // include attribute position
    int last = first + getLength ();

    // normalise first and last positions
    while (first < 0)
      first += ScreenLocation.screenLocations;
    while (last > ScreenLocation.screenLocations)
      last -= ScreenLocation.screenLocations;

    if (first <= last)
      return position.location >= first && position.location <= last;
    return position.location >= first || position.location <= last;
  }

  private String rightTrim ()
  {
    String data = getFieldValue ();
    if (data == null || data.isEmpty ())
      return "";
    int ptr = data.length () - 1;
    while (ptr >= 0 && (data.charAt (ptr) == 0 || data.charAt (ptr) == 0x20))
      ptr--;
    return data.substring (0, ptr + 1);
  }

  // ----------------------------------------------------------------------------------//
  // fieldValue property
  // ----------------------------------------------------------------------------------//

  public void setFieldValue (String value)
  {
    fieldValueProperty ().set (value);
  }

  public String getFieldValue ()
  {
    return fieldValueProperty ().get ();
  }

  public StringProperty fieldValueProperty ()
  {
    if (fieldValue == null)
      fieldValue = new SimpleStringProperty (this, "fieldValue");
    return fieldValue;
  }

  // ----------------------------------------------------------------------------------//
  // modifiable property
  // ----------------------------------------------------------------------------------//

  public void setModifiable (String value)
  {
    modifiableeProperty ().set (value);
  }

  public String getModifiable ()
  {
    return modifiableeProperty ().get ();
  }

  public StringProperty modifiableeProperty ()
  {
    if (modifiable == null)
      modifiable = new SimpleStringProperty (this, "modifiable");
    return modifiable;
  }

  // ----------------------------------------------------------------------------------//
  // visible property
  // ----------------------------------------------------------------------------------//

  public void setVisible (String value)
  {
    visibleProperty ().set (value);
  }

  public String getVisible ()
  {
    return visibleProperty ().get ();
  }

  public StringProperty visibleProperty ()
  {
    if (visible == null)
      visible = new SimpleStringProperty (this, "visible");
    return visible;
  }

  // ----------------------------------------------------------------------------------//
  // altered property
  // ----------------------------------------------------------------------------------//

  public void setAltered (String value)
  {
    alteredProperty ().set (value);
  }

  public String getAltered ()
  {
    return alteredProperty ().get ();
  }

  public StringProperty alteredProperty ()
  {
    if (altered == null)
      altered = new SimpleStringProperty (this, "altered");
    return altered;
  }

  // ----------------------------------------------------------------------------------//
  // alpha property
  // ----------------------------------------------------------------------------------//

  public void setFormat (String value)
  {
    formatProperty ().set (value);
  }

  public String getFormat ()
  {
    return formatProperty ().get ();
  }

  public StringProperty formatProperty ()
  {
    if (format == null)
      format = new SimpleStringProperty (this, "format");
    return format;
  }

  // ----------------------------------------------------------------------------------//
  // sequence property
  // ----------------------------------------------------------------------------------//

  public void setSequence (Integer value)
  {
    sequenceProperty ().set (value);
  }

  public Integer getSequence ()
  {
    return sequenceProperty ().get ();
  }

  public IntegerProperty sequenceProperty ()
  {
    if (sequenceValue == null)
      sequenceValue = new SimpleIntegerProperty (this, "sequence");
    return sequenceValue;
  }

  // ----------------------------------------------------------------------------------//
  // row property
  // ----------------------------------------------------------------------------------//

  public void setRow (Integer value)
  {
    rowProperty ().set (value);
  }

  public Integer getRow ()
  {
    return rowProperty ().get ();
  }

  public IntegerProperty rowProperty ()
  {
    if (row == null)
      row = new SimpleIntegerProperty (this, "row");
    return row;
  }

  // ----------------------------------------------------------------------------------//
  // column property
  // ----------------------------------------------------------------------------------//

  public void setColumn (Integer value)
  {
    columnProperty ().set (value);
  }

  public Integer getColumn ()
  {
    return columnProperty ().get ();
  }

  public IntegerProperty columnProperty ()
  {
    if (column == null)
      column = new SimpleIntegerProperty (this, "column");
    return column;
  }

  // ----------------------------------------------------------------------------------//
  // length property
  // ----------------------------------------------------------------------------------//

  public void setLength (Integer value)
  {
    lengthProperty ().set (value);
  }

  public Integer getLength ()
  {
    return lengthProperty ().get ();
  }

  public IntegerProperty lengthProperty ()
  {
    if (lengthValue == null)
      lengthValue = new SimpleIntegerProperty (this, "length");
    return lengthValue;
  }

  @Override
  public String toString ()
  {
    String trim = rightTrim ();
    String left1 =
        String.format ("%2d  %2d  %4d  ", location.row, location.column, getLength ());
    String left2 = "              ";
    StringBuilder text = new StringBuilder (left1);
    while (trim.length () > 40)
    {
      text.append (trim.substring (0, 40));
      text.append ("\n");
      text.append (left2);
      trim = trim.substring (40);
    }
    text.append (trim);

    return text.toString ();
  }
}