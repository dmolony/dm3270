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
    addColumnString ("Time", 100, Justification.CENTER, "time");
    addColumnString ("System", 100, Justification.LEFT, "system");
    addColumnString ("Subsystem", 100, Justification.LEFT, "subsystem");
    addColumnString ("Message", 500, Justification.LEFT, "firstLine");

    setItems (messages);

    setPlaceholder (new Label ("No messages have been logged"));
  }

  public void addConsoleMessage (ConsoleMessage consoleMessage)
  {
    //    if (messages.stream ().noneMatch (b -> b.matches (consoleMessage)))
    messages.add (consoleMessage);
  }
}
