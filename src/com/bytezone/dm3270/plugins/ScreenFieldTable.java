package com.bytezone.dm3270.plugins;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class ScreenFieldTable extends TableView<ScreenField>
{
  public ScreenFieldTable ()
  {
    setStyle ("-fx-font-size: 11; -fx-font-family: Monospaced");
    setFixedCellSize (20.0);

    TableColumn<ScreenField, Integer> row = new TableColumn<> ("Row");
    row.setPrefWidth (50);
    row.setCellValueFactory (new PropertyValueFactory<> ("row"));

    TableColumn<ScreenField, Integer> column = new TableColumn<> ("Column");
    column.setPrefWidth (50);
    column.setCellValueFactory (new PropertyValueFactory<> ("column"));

    TableColumn<ScreenField, Integer> length = new TableColumn<> ("Length");
    length.setPrefWidth (80);
    length.setCellValueFactory (new PropertyValueFactory<> ("length"));

    TableColumn<ScreenField, String> fieldValue = new TableColumn<> ("Field value");
    fieldValue.setPrefWidth (600);
    fieldValue.setCellValueFactory (new PropertyValueFactory<> ("fieldValue"));

    getColumns ().setAll (row, column, length, fieldValue);

    Callback<TableColumn<ScreenField, Integer>, //
    TableCell<ScreenField, Integer>> rightJustified =
        new Callback<TableColumn<ScreenField, Integer>, //
        TableCell<ScreenField, Integer>> ()
        {
          @Override
          public TableCell<ScreenField, Integer>
              call (TableColumn<ScreenField, Integer> p)
          {
            TableCell<ScreenField, Integer> cell = new TableCell<ScreenField, Integer> ()
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

            cell.setStyle ("-fx-alignment: center-right;");
            return cell;
          }
        };

    row.setCellFactory (rightJustified);
    column.setCellFactory (rightJustified);
    length.setCellFactory (rightJustified);
  }
}