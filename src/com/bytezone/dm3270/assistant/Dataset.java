package com.bytezone.dm3270.assistant;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Dataset
{
  private StringProperty datasetNameProperty;
  private IntegerProperty tracksProperty;
  private IntegerProperty cylindersProperty;
  private IntegerProperty extentsProperty;
  private IntegerProperty percentUsedProperty;
  private StringProperty volumeProperty;
  private StringProperty deviceProperty;
  private StringProperty dsorgProperty;
  private StringProperty recfmProperty;
  private IntegerProperty lreclProperty;
  private IntegerProperty blksizeProperty;
  private StringProperty catalogProperty;
  private StringProperty createdProperty;
  private StringProperty expiresProperty;
  private StringProperty referredProperty;

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
    if (other.getCylinders () > 0)
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

  // ---------------------------------------------------------------------------------//
  // DatasetName
  // ---------------------------------------------------------------------------------//
  public final void setDatasetName (String value)
  {
    datasetNameProperty ().set (value);
  }

  public final String getDatasetName ()
  {
    return datasetNameProperty ().get ();
  }

  public final StringProperty datasetNameProperty ()
  {
    if (datasetNameProperty == null)
      datasetNameProperty = new SimpleStringProperty ();
    return datasetNameProperty;
  }

  // ---------------------------------------------------------------------------------//
  // Tracks
  // ---------------------------------------------------------------------------------//
  public final void setTracks (int value)
  {
    tracksProperty ().setValue (value);
  }

  public final int getTracks ()
  {
    return tracksProperty ().getValue ();
  }

  public final IntegerProperty tracksProperty ()
  {
    if (tracksProperty == null)
      tracksProperty = new SimpleIntegerProperty ();
    return tracksProperty;
  }

  // ---------------------------------------------------------------------------------//
  // Cylinders
  // ---------------------------------------------------------------------------------//
  public final void setCylinders (int value)
  {
    cylindersProperty ().set (value);
  }

  public final int getCylinders ()
  {
    return cylindersProperty ().get ();
  }

  public final IntegerProperty cylindersProperty ()
  {
    if (cylindersProperty == null)
      cylindersProperty = new SimpleIntegerProperty ();
    return cylindersProperty;
  }

  // ---------------------------------------------------------------------------------//
  // Extents
  // ---------------------------------------------------------------------------------//
  public final void setExtents (int value)
  {
    extentsProperty ().setValue (value);
  }

  public final int getExtents ()
  {
    return extentsProperty ().getValue ();
  }

  public final IntegerProperty extentsProperty ()
  {
    if (extentsProperty == null)
      extentsProperty = new SimpleIntegerProperty ();
    return extentsProperty;
  }

  // ---------------------------------------------------------------------------------//
  // PercentUsed
  // ---------------------------------------------------------------------------------//
  public final void setPercentUsed (int value)
  {
    percentUsedProperty ().setValue (value);
  }

  public final int getPercentUsed ()
  {
    return percentUsedProperty ().getValue ();
  }

  public final IntegerProperty percentUsedProperty ()
  {
    if (percentUsedProperty == null)
      percentUsedProperty = new SimpleIntegerProperty ();
    return percentUsedProperty;
  }

  // ---------------------------------------------------------------------------------//
  // Volume
  // ---------------------------------------------------------------------------------//
  public final void setVolume (String value)
  {
    volumeProperty ().set (value);
  }

  public final String getVolume ()
  {
    return volumeProperty ().get ();
  }

  public final StringProperty volumeProperty ()
  {
    if (volumeProperty == null)
      volumeProperty = new SimpleStringProperty ();
    return volumeProperty;
  }

  // ---------------------------------------------------------------------------------//
  // Device
  // ---------------------------------------------------------------------------------//
  public final void setDevice (String value)
  {
    deviceProperty ().set (value);
  }

  public final String getDevice ()
  {
    return deviceProperty ().get ();
  }

  public final StringProperty deviceProperty ()
  {
    if (deviceProperty == null)
      deviceProperty = new SimpleStringProperty ();
    return deviceProperty;
  }

  // ---------------------------------------------------------------------------------//
  // Dsorg
  // ---------------------------------------------------------------------------------//
  public final void setDsorg (String value)
  {
    dsorgProperty ().set (value);
  }

  public final String getDsorg ()
  {
    return dsorgProperty ().get ();
  }

  public final StringProperty dsorgProperty ()
  {
    if (dsorgProperty == null)
      dsorgProperty = new SimpleStringProperty ();
    return dsorgProperty;
  }

  // ---------------------------------------------------------------------------------//
  // Recfm
  // ---------------------------------------------------------------------------------//
  public final void setRecfm (String value)
  {
    recfmProperty ().set (value);
  }

  public final String getRecfm ()
  {
    return recfmProperty ().get ();
  }

  public final StringProperty recfmProperty ()
  {
    if (recfmProperty == null)
      recfmProperty = new SimpleStringProperty ();
    return recfmProperty;
  }

  // ---------------------------------------------------------------------------------//
  // Lrecl
  // ---------------------------------------------------------------------------------//
  public final void setLrecl (int value)
  {
    lreclProperty ().setValue (value);
  }

  public final int getLrecl ()
  {
    return lreclProperty ().getValue ();
  }

  public final IntegerProperty lreclProperty ()
  {
    if (lreclProperty == null)
      lreclProperty = new SimpleIntegerProperty ();
    return lreclProperty;
  }

  // ---------------------------------------------------------------------------------//
  // Blksize
  // ---------------------------------------------------------------------------------//
  public final void setBlksize (int value)
  {
    blksizeProperty ().setValue (value);
  }

  public final int getBlksize ()
  {
    return blksizeProperty ().getValue ();
  }

  public final IntegerProperty blksizeProperty ()
  {
    if (blksizeProperty == null)
      blksizeProperty = new SimpleIntegerProperty ();
    return blksizeProperty;
  }

  // ---------------------------------------------------------------------------------//
  // Catalog
  // ---------------------------------------------------------------------------------//
  public final void setCatalog (String value)
  {
    catalogProperty ().set (value);
  }

  public final String getCatalog ()
  {
    return catalogProperty ().get ();
  }

  public final StringProperty catalogProperty ()
  {
    if (catalogProperty == null)
      catalogProperty = new SimpleStringProperty ();
    return catalogProperty;
  }

  // ---------------------------------------------------------------------------------//
  // Created
  // ---------------------------------------------------------------------------------//
  public final void setCreated (String value)
  {
    createdProperty ().set (value);
  }

  public final String getCreated ()
  {
    return createdProperty ().get ();
  }

  public final StringProperty createdProperty ()
  {
    if (createdProperty == null)
      createdProperty = new SimpleStringProperty ();
    return createdProperty;
  }

  // ---------------------------------------------------------------------------------//
  // Expires
  // ---------------------------------------------------------------------------------//
  public final void setExpires (String value)
  {
    expiresProperty ().set (value);
  }

  public final String getExpires ()
  {
    return expiresProperty ().get ();
  }

  public final StringProperty expiresProperty ()
  {
    if (expiresProperty == null)
      expiresProperty = new SimpleStringProperty ();
    return expiresProperty;
  }

  // ---------------------------------------------------------------------------------//
  // Referred
  // ---------------------------------------------------------------------------------//
  public final void setReferred (String value)
  {
    referredProperty ().set (value);
  }

  public final String getReferred ()
  {
    return referredProperty ().get ();
  }

  public final StringProperty referredProperty ()
  {
    if (referredProperty == null)
      referredProperty = new SimpleStringProperty ();
    return referredProperty;
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