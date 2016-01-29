package com.bytezone.dm3270.console;

import com.bytezone.dm3270.utilities.DefaultTable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;

public class ConsoleMessageTable extends DefaultTable<ConsoleMessage>
{
  final ObservableList<ConsoleMessage> messages = FXCollections.observableArrayList ();

  public ConsoleMessageTable ()
  {
    addColumnString ("Time", 100, Justification.CENTER, "time");
    addColumnString ("System", 80, Justification.CENTER, "system");
    addColumnString ("Task", 80, Justification.CENTER, "task");
    addColumnString ("Rsp", 30, Justification.CENTER, "respond");
    addColumnString ("Code", 80, Justification.LEFT, "messageCode");
    addColumnString ("Message", 900, Justification.LEFT, "message");

    setPlaceholder (new Label ("No messages have been logged"));
  }
}