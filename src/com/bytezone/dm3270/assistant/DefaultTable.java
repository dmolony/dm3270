package com.bytezone.dm3270.assistant;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public abstract class DefaultTable<T> extends TableView<T>
{
  enum Justification
  {
    LEFT, CENTER, RIGHT
  }

  public DefaultTable ()
  {
    setStyle ("-fx-font-size: 12; -fx-font-family: Monospaced");
    setFixedCellSize (20.0);
  }

  // NB propertyName must have a corresponding method xxxProperty() in T
  protected void addColumnString (String heading, int width, Justification justification,
      String propertyName)
  {
    TableColumn<T, String> column = new TableColumn<> (heading);
    column.setPrefWidth (width);
    column.setCellValueFactory (new PropertyValueFactory<T, String> (propertyName));
    getColumns ().add (column);

    if (justification == Justification.CENTER)
      column.setStyle ("-fx-alignment: CENTER;");
  }

  // NB propertyName must have a corresponding method xxxProperty() in T
  protected void addColumnNumber (String heading, int width, String propertyName)
  {
    TableColumn<T, Number> column = new TableColumn<> (heading);
    column.setPrefWidth (width);
    column.setCellValueFactory (new PropertyValueFactory<T, Number> (propertyName));
    getColumns ().add (column);
    column.setStyle ("-fx-alignment: CENTER-RIGHT;");
  }
}