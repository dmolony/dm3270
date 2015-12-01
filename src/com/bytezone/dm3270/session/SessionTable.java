package com.bytezone.dm3270.session;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class SessionTable extends TableView<SessionRecord>
{
  public SessionTable ()
  {
    setStyle ("-fx-font-size: 11;");
    setFixedCellSize (20.0);

    TableColumn<SessionRecord, String> timeCol = new TableColumn<> ("mm:ss");
    TableColumn<SessionRecord, String> sourceCol = new TableColumn<> ("Source");
    TableColumn<SessionRecord, String> commandTypeCol = new TableColumn<> ("Type");
    TableColumn<SessionRecord, String> commandCol = new TableColumn<> ("Command");
    TableColumn<SessionRecord, Integer> sizeCol = new TableColumn<> ("Size");

    timeCol.setPrefWidth (42);
    timeCol.setCellValueFactory (new PropertyValueFactory<> ("time"));

    sourceCol.setPrefWidth (55);
    sourceCol.setCellValueFactory (new PropertyValueFactory<> ("sourceName"));

    commandTypeCol.setPrefWidth (60);
    commandTypeCol.setCellValueFactory (new PropertyValueFactory<> ("commandType"));

    commandCol.setPrefWidth (70);
    commandCol.setCellValueFactory (new PropertyValueFactory<> ("commandName"));

    sizeCol.setPrefWidth (60);
    sizeCol.setCellValueFactory (new PropertyValueFactory<> ("bufferSize"));

    getColumns ().add (timeCol);
    getColumns ().add (sourceCol);
    getColumns ().add (commandTypeCol);
    getColumns ().add (commandCol);
    getColumns ().add (sizeCol);

    Callback<TableColumn<SessionRecord, Integer>, //
    TableCell<SessionRecord, Integer>> rightJustified =
        new Callback<TableColumn<SessionRecord, Integer>, //
        TableCell<SessionRecord, Integer>> ()
        {
          @Override
          public TableCell<SessionRecord, Integer>
              call (TableColumn<SessionRecord, Integer> p)
          {
            TableCell<SessionRecord, Integer> cell =
                new TableCell<SessionRecord, Integer> ()
            {
              @Override
              public void updateItem (Integer item, boolean empty)
              {
                super.updateItem (item, empty);
                setText (empty ? null : String.format ("%,d    ", getItem ()));
                setGraphic (null);
              }
            };

            cell.setStyle ("-fx-alignment: center-right;");
            return cell;
          }
        };

    Callback<TableColumn<SessionRecord, String>, //
    TableCell<SessionRecord, String>> centreJustified =
        new Callback<TableColumn<SessionRecord, String>, //
        TableCell<SessionRecord, String>> ()
        {
          @Override
          public TableCell<SessionRecord, String>
              call (TableColumn<SessionRecord, String> p)
          {
            TableCell<SessionRecord, String> cell =
                new TableCell<SessionRecord, String> ()
            {
              @Override
              public void updateItem (String item, boolean empty)
              {
                super.updateItem (item, empty);
                setText (empty ? null : getItem () == null ? "" : getItem ().toString ());
                setGraphic (null);
              }
            };

            cell.setStyle ("-fx-alignment: center;");
            return cell;
          }
        };

    sizeCol.setCellFactory (rightJustified);
    sourceCol.setCellFactory (centreJustified);
    timeCol.setCellFactory (centreJustified);
  }
}