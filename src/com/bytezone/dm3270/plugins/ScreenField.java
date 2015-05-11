package com.bytezone.dm3270.plugins;

public class ScreenField
{
  private PluginData pluginData;

  public final ScreenLocation location;
  public final int length;
  public final boolean isProtected;
  public final boolean isModifiable;
  public final boolean isAlpha;
  public final String data;

  public String newData;

  public ScreenField (int location, int row, int column, int length, boolean isProtected,
      boolean isAlpha, String data)
  {
    this.location = new ScreenLocation (location);
    this.length = length;
    this.isProtected = isProtected;
    this.isModifiable = !isProtected;
    this.isAlpha = isAlpha;
    this.data = data;
  }

  public boolean isModifiableLength (int length)
  {
    return !isProtected && this.length == length;
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

  public boolean contains (ScreenLocation position)
  {
    if (location.location == position.location)
      return true;

    int first = location.location - 1;      // include attribute position
    int last = first + length;

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
    if (data == null || data.isEmpty ())
      return "";
    int ptr = data.length () - 1;
    while (ptr >= 0 && (data.charAt (ptr) == 0 || data.charAt (ptr) == 0x20))
      ptr--;
    return data.substring (0, ptr + 1);
  }

  @Override
  public String toString ()
  {
    String trim = rightTrim ();
    String left1 =
        String.format ("%2d  %2d  %4d  ", location.row, location.column, length);
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