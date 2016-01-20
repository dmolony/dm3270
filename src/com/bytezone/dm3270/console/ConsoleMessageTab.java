package com.bytezone.dm3270.console;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
  private final TextField txtMessageCode = new TextField ();

  public ConsoleMessageTab ()
  {
    super ("Filters");

    setClosable (false);
    consoleMessageTable.getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, newSelection) -> select (newSelection));

    HBox box = new HBox (10);                         // spacing
    box.setPadding (new Insets (10, 10, 10, 10));     // trbl
    box.setAlignment (Pos.CENTER_LEFT);

    Label lblSubsytem = new Label ("Subsystem");
    Label lblMessageCode = new Label ("Code");
    box.getChildren ().addAll (lblSubsytem, txtSubsystem, lblMessageCode, txtMessageCode);

    BorderPane borderPane = new BorderPane ();
    borderPane.setTop (box);
    borderPane.setCenter (consoleMessageTable);

    setContent (borderPane);

    FilteredList<ConsoleMessage> filteredData =
        new FilteredList<> (consoleMessageTable.messages, m -> true);
    txtSubsystem.textProperty ()
        .addListener ( (observable, oldValue, newValue) -> setFilter (filteredData));
    txtMessageCode.textProperty ()
        .addListener ( (observable, oldValue, newValue) -> setFilter (filteredData));

    SortedList<ConsoleMessage> sortedData = new SortedList<> (filteredData);
    sortedData.comparatorProperty ().bind (consoleMessageTable.comparatorProperty ());

    consoleMessageTable.setItems (sortedData);
  }

  private void setFilter (FilteredList<ConsoleMessage> filteredData)
  {
    String subsystem = txtSubsystem.getText ();
    String code = txtMessageCode.getText ();

    filteredData.setPredicate (message ->
    {
      boolean p1 = message.getSubsystem ().startsWith (subsystem);
      boolean p2 = message.getMessageCode ().startsWith (code);
      return p1 && p2;
    });
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