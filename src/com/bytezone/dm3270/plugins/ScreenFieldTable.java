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

    TableColumn<ScreenField, Integer> sequence = new TableColumn<> ("Seq");
    sequence.setPrefWidth (50);
    sequence.setCellValueFactory (new PropertyValueFactory<> ("sequence"));

    TableColumn<ScreenField, Integer> row = new TableColumn<> ("Row");
    row.setPrefWidth (50);
    row.setCellValueFactory (new PropertyValueFactory<> ("row"));

    TableColumn<ScreenField, Integer> column = new TableColumn<> ("Column");
    column.setPrefWidth (50);
    column.setCellValueFactory (new PropertyValueFactory<> ("column"));

    TableColumn<ScreenField, Integer> length = new TableColumn<> ("Length");
    length.setPrefWidth (80);
    length.setCellValueFactory (new PropertyValueFactory<> ("length"));

    TableColumn<ScreenField, String> modifiable = new TableColumn<> ("Modifiable");
    modifiable.setPrefWidth (80);
    modifiable.setCellValueFactory (new PropertyValueFactory<> ("modifiable"));

    TableColumn<ScreenField, String> visible = new TableColumn<> ("Visible");
    visible.setPrefWidth (80);
    visible.setCellValueFactory (new PropertyValueFactory<> ("visible"));

    TableColumn<ScreenField, String> altered = new TableColumn<> ("Altered");
    altered.setPrefWidth (80);
    altered.setCellValueFactory (new PropertyValueFactory<> ("altered"));

    TableColumn<ScreenField, String> format = new TableColumn<> ("Format");
    format.setPrefWidth (50);
    format.setCellValueFactory (new PropertyValueFactory<> ("format"));

    TableColumn<ScreenField, String> fieldValue = new TableColumn<> ("Field value");
    fieldValue.setPrefWidth (600);
    fieldValue.setCellValueFactory (new PropertyValueFactory<> ("fieldValue"));

    getColumns ().setAll (sequence, row, column, length, format, visible, modifiable,
                          altered, fieldValue);

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

    Callback<TableColumn<ScreenField, String>, //
    TableCell<ScreenField, String>> centreJustified =
        new Callback<TableColumn<ScreenField, String>, //
        TableCell<ScreenField, String>> ()
        {
          @Override
          public TableCell<ScreenField, String> call (TableColumn<ScreenField, String> p)
          {
            TableCell<ScreenField, String> cell = new TableCell<ScreenField, String> ()
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

    sequence.setCellFactory (rightJustified);
    row.setCellFactory (rightJustified);
    column.setCellFactory (rightJustified);
    length.setCellFactory (rightJustified);
    modifiable.setCellFactory (centreJustified);
    visible.setCellFactory (centreJustified);
    altered.setCellFactory (centreJustified);
    format.setCellFactory (centreJustified);
  }
}