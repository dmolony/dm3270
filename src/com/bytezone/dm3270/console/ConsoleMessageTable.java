package com.bytezone.dm3270.console;

import com.bytezone.dm3270.utilities.DefaultTable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;

public class ConsoleMessageTable extends DefaultTable<ConsoleMessage>
{
  private final ObservableList<ConsoleMessage> messages =
      FXCollections.observableArrayList ();

  public ConsoleMessageTable ()
  {
    addColumnString ("Time", 100, Justification.CENTER, "jobNumber");
    addColumnString ("System", 100, Justification.LEFT, "jobNumber");
    addColumnString ("Subsystem", 100, Justification.LEFT, "jobNumber");
    addColumnString ("Message", 100, Justification.LEFT, "jobNumber");

    setItems (messages);

    setPlaceholder (new Label ("No messages have been logged"));
  }
}
