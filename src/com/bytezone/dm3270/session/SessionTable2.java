package com.bytezone.dm3270.session;

import com.bytezone.dm3270.assistant.DefaultTable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SessionTable2 extends DefaultTable<SessionRecord>
{
  private final ObservableList<SessionRecord> sessionRecords =
      FXCollections.observableArrayList ();

  public SessionTable2 ()
  {
    addColumnString ("mm:ss", 42, Justification.LEFT, "time");
    addColumnString ("Source", 55, Justification.LEFT, "source");
    addColumnString ("Type", 60, Justification.LEFT, "type");
    addColumnString ("Command", 70, Justification.LEFT, "command");
    addColumnNumber ("Size", 60, "size");
  }

  public void addSessionRecord (SessionRecord sessionRecord)
  {
    sessionRecords.add (sessionRecord);
  }
}