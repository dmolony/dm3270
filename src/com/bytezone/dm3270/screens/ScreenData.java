package com.bytezone.dm3270.screens;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.FieldManager;

public class ScreenData
{
  int columns;
  List<Field> fields;
  boolean isIspfScreen;

  public ScreenData (FieldManager fieldManager)
  {
    this.columns = fieldManager.getScreenColumns ();
  }

  protected List<Field> getFieldsOnRow (List<Field> fields, int requestedRow)
  {
    int firstLocation = requestedRow * columns;
    int lastLocation = firstLocation + columns - 1;
    return getFields (fields, firstLocation, lastLocation);
  }

  protected List<Field> getFieldsOnRows (List<Field> fields, int requestedRowFrom,
      int rows)
  {
    int firstLocation = requestedRowFrom * columns;
    int lastLocation = (requestedRowFrom + rows) * columns - 1;
    return getFields (fields, firstLocation, lastLocation);
  }

  private List<Field> getFields (List<Field> fields, int firstLocation, int lastLocation)
  {
    List<Field> rowFields = new ArrayList<> ();
    for (Field field : fields)
    {
      int location = field.getFirstLocation ();
      if (location < firstLocation)
        continue;
      if (location > lastLocation)
        break;
      if (field.getDisplayLength () > 0)
        rowFields.add (field);
    }
    return rowFields;
  }
}