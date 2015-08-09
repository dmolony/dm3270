package com.bytezone.dm3270.datasets;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Dataset
{
  private StringProperty propertyDatasetName;
  private IntegerProperty propertyTracks;
  private IntegerProperty propertyCylinders;
  private IntegerProperty propertyExtents;
  private IntegerProperty propertyPercentUsed;
  private StringProperty propertyVolume;

  public Dataset (String name)
  {
    propertyDatasetName ().set (name);
  }

  // DatasetName

  public void setDatasetName (String value)
  {
    propertyDatasetName ().set (value);
  }

  public String getDatasetName ()
  {
    return propertyDatasetName ().get ();
  }

  public StringProperty propertyDatasetName ()
  {
    if (propertyDatasetName == null)
      propertyDatasetName = new SimpleStringProperty (this, "DatasetName");
    return propertyDatasetName;
  }

  // Tracks

  public void setTracks (int value)
  {
    propertyTracks ().set (value);
  }

  public int getTracks ()
  {
    return propertyTracks ().get ();
  }

  public IntegerProperty propertyTracks ()
  {
    if (propertyTracks == null)
      propertyTracks = new SimpleIntegerProperty (this, "Tracks");
    return propertyTracks;
  }

  // Cylinders

  public void setCylinders (int value)
  {
    propertyCylinders ().set (value);
  }

  public int getCylinders ()
  {
    return propertyCylinders ().get ();
  }

  public IntegerProperty propertyCylinders ()
  {
    if (propertyCylinders == null)
      propertyCylinders = new SimpleIntegerProperty (this, "Cylinders");
    return propertyCylinders;
  }

  // Extents

  public void setExtents (int value)
  {
    propertyExtents ().set (value);
  }

  public int getExtents ()
  {
    return propertyExtents ().get ();
  }

  public IntegerProperty propertyExtents ()
  {
    if (propertyExtents == null)
      propertyExtents = new SimpleIntegerProperty (this, "Extents");
    return propertyExtents;
  }

  // PercentUsed

  public void setPercentUsed (int value)
  {
    propertyPercentUsed ().set (value);
  }

  public int getPercentUsed ()
  {
    return propertyPercentUsed ().get ();
  }

  public IntegerProperty propertyPercentUsed ()
  {
    if (propertyPercentUsed == null)
      propertyPercentUsed = new SimpleIntegerProperty (this, "PercentUsed");
    return propertyPercentUsed;
  }

  // Volume

  public void setVolume (String value)
  {
    propertyVolume ().set (value);
  }

  public String getVolume ()
  {
    return propertyVolume ().get ();
  }

  public StringProperty propertyVolume ()
  {
    if (propertyVolume == null)
      propertyVolume = new SimpleStringProperty (this, "Volume");
    return propertyVolume;
  }
}