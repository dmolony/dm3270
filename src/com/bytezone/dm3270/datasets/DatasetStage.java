package com.bytezone.dm3270.datasets;

import com.bytezone.dm3270.display.ScreenDetails;
import com.bytezone.dm3270.display.TSOCommandStatusListener;

import javafx.stage.Stage;

public class DatasetStage extends Stage implements TSOCommandStatusListener
{

  public DatasetStage ()
  {
    setTitle ("Datasets");
  }

  @Override
  public void screenChanged (ScreenDetails screenDetails)
  {
  }
}