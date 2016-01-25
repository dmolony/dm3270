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
  private final TextField txtTask = new TextField ();
  private final TextField txtMessageCode = new TextField ();
  private final TextField txtMessageText = new TextField ();
  private final TextField txtTime = new TextField ();

  public ConsoleMessageTab ()
  {
    super ("Filters");

    setClosable (false);
    consoleMessageTable.getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, newSelection) -> select (newSelection));

    HBox box = new HBox (10);                         // spacing
    box.setPadding (new Insets (10, 10, 10, 10));     // trbl
    box.setAlignment (Pos.CENTER_LEFT);

    Label lblTime = new Label ("Time");
    Label lblSubsytem = new Label ("Task");
    Label lblMessageCode = new Label ("Code");
    Label lblMessageText = new Label ("Text");
    box.getChildren ().addAll (lblTime, txtTime, lblSubsytem, txtTask, lblMessageCode,
                               txtMessageCode, lblMessageText, txtMessageText);

    BorderPane borderPane = new BorderPane ();
    borderPane.setTop (box);
    borderPane.setCenter (consoleMessageTable);

    setContent (borderPane);

    FilteredList<ConsoleMessage> filteredData =
        new FilteredList<> (consoleMessageTable.messages, m -> true);

    SortedList<ConsoleMessage> sortedData = new SortedList<> (filteredData);
    sortedData.comparatorProperty ().bind (consoleMessageTable.comparatorProperty ());

    consoleMessageTable.setItems (sortedData);

    txtTime.textProperty ()
        .addListener ( (observable, oldValue, newValue) -> setFilter (filteredData));
    txtTask.textProperty ()
        .addListener ( (observable, oldValue, newValue) -> setFilter (filteredData));
    txtMessageCode.textProperty ()
        .addListener ( (observable, oldValue, newValue) -> setFilter (filteredData));
    txtMessageText.textProperty ()
        .addListener ( (observable, oldValue, newValue) -> setFilter (filteredData));
  }

  private void setFilter (FilteredList<ConsoleMessage> filteredData)
  {
    String time = txtTime.getText ();
    String task = txtTask.getText ();
    String code = txtMessageCode.getText ();
    String text = txtMessageText.getText ();

    filteredData.setPredicate (message ->
    {
      boolean p0 = message.getTime ().startsWith (time);
      boolean p1 = message.getTask ().startsWith (task);
      boolean p2 = message.getMessageCode ().startsWith (code);
      boolean p3 = message.getMessage ().contains (text);
      return p0 && p1 && p2 && p3;
    });
  }

  @Override
  public void consoleMessage (ConsoleMessage consoleMessage)
  {
    consoleMessageTable.messages.add (consoleMessage);
  }

  private void select (ConsoleMessage consoleMessage)
  {
    if (consoleMessage == null)
      return;

    System.out.println (consoleMessage);
  }
}