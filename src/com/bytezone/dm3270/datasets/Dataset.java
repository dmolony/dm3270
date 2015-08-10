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
  private StringProperty propertyCatalog;
  private StringProperty propertyCreated;
  private StringProperty propertyExpires;
  private StringProperty propertyReferred;

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
    if (other.getCatalog () != null)
      setCatalog (other.getCatalog ());
    if (other.getCreated () != null)
      setCreated (other.getCreated ());
    if (other.getExpires () != null)
      setExpires (other.getExpires ());
    if (other.getReferred () != null)
      setReferred (other.getReferred ());
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

  // Catalog

  public void setCatalog (String value)
  {
    propertyCatalog ().set (value);
  }

  public String getCatalog ()
  {
    return propertyCatalog ().get ();
  }

  public StringProperty propertyCatalog ()
  {
    if (propertyCatalog == null)
      propertyCatalog = new SimpleStringProperty (this, "Catalog");
    return propertyCatalog;
  }

  // Created

  public void setCreated (String value)
  {
    propertyCreated ().set (value);
  }

  public String getCreated ()
  {
    return propertyCreated ().get ();
  }

  public StringProperty propertyCreated ()
  {
    if (propertyCreated == null)
      propertyCreated = new SimpleStringProperty (this, "Created");
    return propertyCreated;
  }

  // Expires

  public void setExpires (String value)
  {
    propertyExpires ().set (value);
  }

  public String getExpires ()
  {
    return propertyExpires ().get ();
  }

  public StringProperty propertyExpires ()
  {
    if (propertyExpires == null)
      propertyExpires = new SimpleStringProperty (this, "Expires");
    return propertyExpires;
  }

  // Referred

  public void setReferred (String value)
  {
    propertyReferred ().set (value);
  }

  public String getReferred ()
  {
    return propertyReferred ().get ();
  }

  public StringProperty propertyReferred ()
  {
    if (propertyReferred == null)
      propertyReferred = new SimpleStringProperty (this, "Referred");
    return propertyReferred;
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
    text.append (String.format ("BLKSIZE ......... %s%n", getBlksize ()));
    text.append (String.format ("Created ......... %s%n", getCreated ()));
    text.append (String.format ("Expires ......... %s%n", getExpires ()));
    text.append (String.format ("Referred ........ %s%n", getReferred ()));
    text.append (String.format ("Catalog ......... %s", getCatalog ()));

    return text.toString ();
  }
}