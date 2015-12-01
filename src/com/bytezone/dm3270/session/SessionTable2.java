package com.bytezone.dm3270.session;

import com.bytezone.dm3270.assistant.DefaultTable;

public class SessionTable2 extends DefaultTable<SessionRecord>
{
  public SessionTable2 ()
  {
    addColumnString ("mm:ss", 45, Justification.LEFT, "time");
    addColumnString ("Source", 55, Justification.LEFT, "sourceName");
    addColumnString ("Type", 70, Justification.LEFT, "commandType");
    addColumnString ("Command", 80, Justification.LEFT, "commandName");
    addColumnNumber ("Size", 55, "bufferSize");
  }
}