package com.bytezone.dm3270.datasets;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Dataset
{
  private StringProperty propertyDatasetName;

  // DatasetName

  public void setDatasetName (String value)
  {
    propertyDatasetName ().set (value);
  }

  public String getJobName ()
  {
    return propertyDatasetName ().get ();
  }

  public StringProperty propertyDatasetName ()
  {
    if (propertyDatasetName == null)
      propertyDatasetName = new SimpleStringProperty (this, "DatasetName");
    return propertyDatasetName;
  }
}