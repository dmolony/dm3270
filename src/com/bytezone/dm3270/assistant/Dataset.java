package com.bytezone.dm3270.assistant;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Dataset
{
  private StringProperty propertyDatasetName;
  private IntegerProperty propertyTracks;
  private StringProperty propertyCylinders;
  private IntegerProperty propertyExtents;
  private IntegerProperty propertyPercentUsed;
  private StringProperty propertyVolume;
  private StringProperty propertyDevice;
  private StringProperty propertyDsorg;
  private StringProperty propertyRecfm;
  private IntegerProperty propertyLrecl;
  private IntegerProperty propertyBlksize;
  private StringProperty propertyCatalog;
  private StringProperty propertyCreated;
  private StringProperty propertyExpires;
  private StringProperty propertyReferred;

  public Dataset (String name)
  {
    datasetNameProperty ().set (name);
  }

  public void merge (Dataset other)
  {
    if (other.getBlksize () > 0)
      setBlksize (other.getBlksize ());
    if (other.getLrecl () > 0)
      setLrecl (other.getLrecl ());
    if (other.getRecfm () != null)
      setRecfm (other.getRecfm ());
    if (other.getDsorg () != null)
      setDsorg (other.getDsorg ());
    if (other.getDevice () != null)
      setDevice (other.getDevice ());
    if (other.getExtents () > 0)
      setExtents (other.getExtents ());
    if (other.getPercentUsed () > 0)
      setPercentUsed (other.getPercentUsed ());
    if (other.getTracks () > 0)
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
    datasetNameProperty ().set (value);
  }

  public String getDatasetName ()
  {
    return datasetNameProperty ().get ();
  }

  public StringProperty datasetNameProperty ()
  {
    if (propertyDatasetName == null)
      propertyDatasetName = new SimpleStringProperty ();
    return propertyDatasetName;
  }

  // Tracks

  public void setTracks (int value)
  {
    tracksProperty ().setValue (value);
  }

  public int getTracks ()
  {
    return tracksProperty ().getValue ();
  }

  public IntegerProperty tracksProperty ()
  {
    if (propertyTracks == null)
      propertyTracks = new SimpleIntegerProperty ();
    return propertyTracks;
  }

  // Cylinders

  public void setCylinders (String value)
  {
    cylindersProperty ().set (value);
  }

  public String getCylinders ()
  {
    return cylindersProperty ().get ();
  }

  public StringProperty cylindersProperty ()
  {
    if (propertyCylinders == null)
      propertyCylinders = new SimpleStringProperty ();
    return propertyCylinders;
  }

  // Extents

  public void setExtents (int value)
  {
    extentsProperty ().setValue (value);
  }

  public int getExtents ()
  {
    return extentsProperty ().getValue ();
  }

  public IntegerProperty extentsProperty ()
  {
    if (propertyExtents == null)
      propertyExtents = new SimpleIntegerProperty ();
    return propertyExtents;
  }

  // PercentUsed

  public void setPercentUsed (int value)
  {
    percentUsedProperty ().setValue (value);
  }

  public int getPercentUsed ()
  {
    return percentUsedProperty ().getValue ();
  }

  public IntegerProperty percentUsedProperty ()
  {
    if (propertyPercentUsed == null)
      propertyPercentUsed = new SimpleIntegerProperty ();
    return propertyPercentUsed;
  }

  // Volume

  public void setVolume (String value)
  {
    volumeProperty ().set (value);
  }

  public String getVolume ()
  {
    return volumeProperty ().get ();
  }

  public StringProperty volumeProperty ()
  {
    if (propertyVolume == null)
      propertyVolume = new SimpleStringProperty ();
    return propertyVolume;
  }

  // Device

  public void setDevice (String value)
  {
    deviceProperty ().set (value);
  }

  public String getDevice ()
  {
    return deviceProperty ().get ();
  }

  public StringProperty deviceProperty ()
  {
    if (propertyDevice == null)
      propertyDevice = new SimpleStringProperty ();
    return propertyDevice;
  }

  // DSORG

  public void setDsorg (String value)
  {
    dsorgProperty ().set (value);
  }

  public String getDsorg ()
  {
    return dsorgProperty ().get ();
  }

  public StringProperty dsorgProperty ()
  {
    if (propertyDsorg == null)
      propertyDsorg = new SimpleStringProperty ();
    return propertyDsorg;
  }

  // RECFM

  public void setRecfm (String value)
  {
    recfmProperty ().set (value);
  }

  public String getRecfm ()
  {
    return recfmProperty ().get ();
  }

  public StringProperty recfmProperty ()
  {
    if (propertyRecfm == null)
      propertyRecfm = new SimpleStringProperty ();
    return propertyRecfm;
  }

  // LRECL

  public void setLrecl (int value)
  {
    lreclProperty ().setValue (value);
  }

  public int getLrecl ()
  {
    return lreclProperty ().getValue ();
  }

  public IntegerProperty lreclProperty ()
  {
    if (propertyLrecl == null)
      propertyLrecl = new SimpleIntegerProperty ();
    return propertyLrecl;
  }

  // BLKSIZE

  public void setBlksize (int value)
  {
    blksizeProperty ().setValue (value);
  }

  public int getBlksize ()
  {
    return blksizeProperty ().getValue ();
  }

  public IntegerProperty blksizeProperty ()
  {
    if (propertyBlksize == null)
      propertyBlksize = new SimpleIntegerProperty ();
    return propertyBlksize;
  }

  // Catalog

  public void setCatalog (String value)
  {
    catalogProperty ().set (value);
  }

  public String getCatalog ()
  {
    return catalogProperty ().get ();
  }

  public StringProperty catalogProperty ()
  {
    if (propertyCatalog == null)
      propertyCatalog = new SimpleStringProperty ();
    return propertyCatalog;
  }

  // Created

  public void setCreated (String value)
  {
    createdProperty ().set (value);
  }

  public String getCreated ()
  {
    return createdProperty ().get ();
  }

  public StringProperty createdProperty ()
  {
    if (propertyCreated == null)
      propertyCreated = new SimpleStringProperty ();
    return propertyCreated;
  }

  // Expires

  public void setExpires (String value)
  {
    expiresProperty ().set (value);
  }

  public String getExpires ()
  {
    return expiresProperty ().get ();
  }

  public StringProperty expiresProperty ()
  {
    if (propertyExpires == null)
      propertyExpires = new SimpleStringProperty ();
    return propertyExpires;
  }

  // Referred

  public void setReferred (String value)
  {
    referredProperty ().set (value);
  }

  public String getReferred ()
  {
    return referredProperty ().get ();
  }

  public StringProperty referredProperty ()
  {
    if (propertyReferred == null)
      propertyReferred = new SimpleStringProperty ();
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