package com.bytezone.dm3270.console;

import javafx.scene.control.Tab;

public class ConsoleMessageTab extends Tab implements ConsoleMessageListener
{
  private final ConsoleMessageTable consoleMessageTable = new ConsoleMessageTable ();

  public ConsoleMessageTab ()
  {
    super ("Messages");

    setClosable (false);
    consoleMessageTable.getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, newSelection) -> select (newSelection));

    setContent (consoleMessageTable);
  }

  @Override
  public void consoleMessage (ConsoleMessage consoleMessage)
  {
    consoleMessageTable.addConsoleMessage (consoleMessage);
  }

  private void select (ConsoleMessage consoleMessage)
  {
    if (consoleMessage == null)
      return;

    //    selectedBatchJob = batchJob;
    //    setText ();
    //    fireJobSelected (batchJob);
  }
}