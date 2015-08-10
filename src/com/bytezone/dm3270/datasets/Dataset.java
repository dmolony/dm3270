package com.bytezone.dm3270.datasets;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Dataset
{
  private StringProperty propertyDatasetName;
  private StringProperty propertyTracks;
  private StringProperty propertyCylinders;
  private StringProperty propertyExtents;
  private StringProperty propertyPercentUsed;
  private StringProperty propertyVolume;
  private StringProperty propertyDevice;
  private StringProperty propertyDsorg;
  private StringProperty propertyRecfm;
  private StringProperty propertyLrecl;
  private StringProperty propertyBlksize;

  public Dataset (String name)
  {
    propertyDatasetName ().set (name);
  }

  public void merge (Dataset other)
  {
    if (other.getBlksize () != null)
      setBlksize (other.getBlksize ());
    if (other.getLrecl () != null)
      setLrecl (other.getLrecl ());
    if (other.getRecfm () != null)
      setRecfm (other.getRecfm ());
    if (other.getDsorg () != null)
      setDsorg (other.getDsorg ());
    if (other.getDevice () != null)
      setDevice (other.getDevice ());
    if (other.getExtents () != null)
      setExtents (other.getExtents ());
    if (other.getPercentUsed () != null)
      setPercentUsed (other.getPercentUsed ());
    if (other.getTracks () != null)
      setTracks (other.getTracks ());
    if (other.getCylinders () != null)
      setCylinders (other.getCylinders ());
    if (other.getVolume () != null)
      setVolume (other.getVolume ());
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

  public void setTracks (String value)
  {
    propertyTracks ().set (value);
  }

  public String getTracks ()
  {
    return propertyTracks ().get ();
  }

  public StringProperty propertyTracks ()
  {
    if (propertyTracks == null)
      propertyTracks = new SimpleStringProperty (this, "Tracks");
    return propertyTracks;
  }

  // Cylinders

  public void setCylinders (String value)
  {
    propertyCylinders ().set (value);
  }

  public String getCylinders ()
  {
    return propertyCylinders ().get ();
  }

  public StringProperty propertyCylinders ()
  {
    if (propertyCylinders == null)
      propertyCylinders = new SimpleStringProperty (this, "Cylinders");
    return propertyCylinders;
  }

  // Extents

  public void setExtents (String value)
  {
    propertyExtents ().set (value);
  }

  public String getExtents ()
  {
    return propertyExtents ().get ();
  }

  public StringProperty propertyExtents ()
  {
    if (propertyExtents == null)
      propertyExtents = new SimpleStringProperty (this, "Extents");
    return propertyExtents;
  }

  // PercentUsed

  public void setPercentUsed (String value)
  {
    propertyPercentUsed ().set (value);
  }

  public String getPercentUsed ()
  {
    return propertyPercentUsed ().get ();
  }

  public StringProperty propertyPercentUsed ()
  {
    if (propertyPercentUsed == null)
      propertyPercentUsed = new SimpleStringProperty (this, "PercentUsed");
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

  // Device

  public void setDevice (String value)
  {
    propertyDevice ().set (value);
  }

  public String getDevice ()
  {
    return propertyDevice ().get ();
  }

  public StringProperty propertyDevice ()
  {
    if (propertyDevice == null)
      propertyDevice = new SimpleStringProperty (this, "Device");
    return propertyDevice;
  }

  // DSORG

  public void setDsorg (String value)
  {
    propertyDsorg ().set (value);
  }

  public String getDsorg ()
  {
    return propertyDsorg ().get ();
  }

  public StringProperty propertyDsorg ()
  {
    if (propertyDsorg == null)
      propertyDsorg = new SimpleStringProperty (this, "DSORG");
    return propertyDsorg;
  }

  // RECFM

  public void setRecfm (String value)
  {
    propertyRecfm ().set (value);
  }

  public String getRecfm ()
  {
    return propertyRecfm ().get ();
  }

  public StringProperty propertyRecfm ()
  {
    if (propertyRecfm == null)
      propertyRecfm = new SimpleStringProperty (this, "RECFM");
    return propertyRecfm;
  }

  // LRECL

  public void setLrecl (String value)
  {
    propertyLrecl ().set (value);
  }

  public String getLrecl ()
  {
    return propertyLrecl ().get ();
  }

  public StringProperty propertyLrecl ()
  {
    if (propertyLrecl == null)
      propertyLrecl = new SimpleStringProperty (this, "LRECL");
    return propertyLrecl;
  }

  // BLKSIZE

  public void setBlksize (String value)
  {
    propertyBlksize ().set (value);
  }

  public String getBlksize ()
  {
    return propertyBlksize ().get ();
  }

  public StringProperty propertyBlksize ()
  {
    if (propertyBlksize == null)
      propertyBlksize = new SimpleStringProperty (this, "BLKSIZE");
    return propertyBlksize;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Name ............ %s%n", getDatasetName ()));
    text.append (String.format ("Tracks .......... %s%n", getTracks ()));
    text.append (String.format ("Cylinders ....... %s%n", getCylinders ()));
    text.append (String.format ("Extents ......... %s%n", getExtents ()));
    text.append (String.format ("Percent used .... %s%n", getPercentUsed ()));
    text.append (String.format ("Device .......... %s%n", getDevice ()));
    text.append (String.format ("DSORG ........... %s%n", getDsorg ()));
    text.append (String.format ("RECFM ........... %s%n", getRecfm ()));
    text.append (String.format ("LRECL ........... %s%n", getLrecl ()));
    text.append (String.format ("BLKSIZE ......... %s", getBlksize ()));

    return text.toString ();
  }
}