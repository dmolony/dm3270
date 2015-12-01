package com.bytezone.dm3270.session;

import com.bytezone.dm3270.utilities.DefaultTable;

public class SessionTable extends DefaultTable<SessionRecord>
{
  public SessionTable ()
  {
    addColumnString ("mm:ss", 45, Justification.LEFT, "time");
    addColumnString ("Source", 55, Justification.LEFT, "sourceName");
    addColumnString ("Type", 70, Justification.LEFT, "commandType");
    addColumnString ("Command", 80, Justification.LEFT, "commandName");
    addColumnNumber ("Size", 55, "bufferSize");

    setPrefWidth (337);
  }
}