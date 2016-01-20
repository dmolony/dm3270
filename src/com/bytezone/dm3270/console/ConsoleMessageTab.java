package com.bytezone.dm3270.console;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class ConsoleMessageTab extends Tab implements ConsoleMessageListener
{
  private final ConsoleMessageTable consoleMessageTable = new ConsoleMessageTable ();
  private final TextField txtSubsystem = new TextField ();

  public ConsoleMessageTab ()
  {
    super ("Messages");

    setClosable (false);
    consoleMessageTable.getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, newSelection) -> select (newSelection));

    HBox box = new HBox (10);
    box.setPadding (new Insets (10, 10, 10, 10));    // trbl
    box.setAlignment (Pos.CENTER_LEFT);

    Label lblSubsytem = new Label ("Subsystem");
    box.getChildren ().addAll (lblSubsytem, txtSubsystem);

    BorderPane borderPane = new BorderPane ();
    borderPane.setTop (box);
    borderPane.setCenter (consoleMessageTable);

    setContent (borderPane);
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

    System.out.println (consoleMessage);
  }
}