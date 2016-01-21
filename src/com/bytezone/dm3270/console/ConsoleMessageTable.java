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
    addColumnString ("System", 80, Justification.LEFT, "system");
    addColumnString ("Subsystem", 80, Justification.LEFT, "subsystem");
    addColumnString ("Code", 80, Justification.LEFT, "messageCode");
    addColumnString ("Message", 1000, Justification.LEFT, "firstLine");

    setPlaceholder (new Label ("No messages have been logged"));
  }

  public void addConsoleMessage (ConsoleMessage consoleMessage)
  {
    messages.add (consoleMessage);
  }
}