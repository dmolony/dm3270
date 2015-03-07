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
    setPrefHeight (770);
    //    setPrefWidth (300);
    setStyle ("-fx-font-size: 11;");
    setFixedCellSize (20.0);

    TableColumn<SessionRecord, String> sourceCol = new TableColumn<> ("Source");
    TableColumn<SessionRecord, String> commandTypeCol = new TableColumn<> ("Type");
    TableColumn<SessionRecord, String> commandCol = new TableColumn<> ("Command");
    TableColumn<SessionRecord, Integer> sizeCol = new TableColumn<> ("Size");

    sourceCol.setPrefWidth (60);
    sourceCol.setCellValueFactory (new PropertyValueFactory<> ("sourceName"));

    commandTypeCol.setPrefWidth (60);
    commandTypeCol.setCellValueFactory (new PropertyValueFactory<> ("commandType"));

    commandCol.setPrefWidth (70);
    commandCol.setCellValueFactory (new PropertyValueFactory<> ("commandName"));

    sizeCol.setPrefWidth (40);
    sizeCol.setCellValueFactory (new PropertyValueFactory<> ("bufferSize"));

    getColumns ().add (sourceCol);
    getColumns ().add (commandTypeCol);
    getColumns ().add (commandCol);
    getColumns ().add (sizeCol);

    setTableMenuButtonVisible (true);

    sizeCol.setCellFactory (new Callback<TableColumn<SessionRecord, Integer>, //
        TableCell<SessionRecord, Integer>> ()
        {
          @Override
          public TableCell<SessionRecord, Integer> call (
              TableColumn<SessionRecord, Integer> p)
          {
            TableCell<SessionRecord, Integer> cell =
                new TableCell<SessionRecord, Integer> ()
                {
                  @Override
                  public void updateItem (Integer item, boolean empty)
                  {
                    super.updateItem (item, empty);
                    setText (empty ? null : getItem () == null ? "0" : //
                        String.format ("%,d", getItem ()));
                    setGraphic (null);
                  }
                };

            cell.setStyle ("-fx-alignment: top-right;");
            return cell;
          }
        });
  }
}