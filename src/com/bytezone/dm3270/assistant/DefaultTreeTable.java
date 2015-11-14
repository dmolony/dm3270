package com.bytezone.dm3270.assistant;

import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

public abstract class DefaultTreeTable<T> extends TreeTableView<T>
{
  enum Justification
  {
    LEFT, CENTER, RIGHT
  }

  public DefaultTreeTable ()
  {
    setStyle ("-fx-font-size: 12; -fx-font-family: Monospaced");
    setFixedCellSize (20.0);
  }

  // NB propertyName must have a corresponding method xxxProperty() in T
  protected void addColumnString (String heading, int width, Justification justification,
      String propertyName)
  {
    TreeTableColumn<T, String> column = new TreeTableColumn<> (heading);
    column.setPrefWidth (width);
    column
        .setCellValueFactory (new TreeItemPropertyValueFactory<T, String> (propertyName));
    getColumns ().add (column);

    if (justification == Justification.CENTER)
      column.setStyle ("-fx-alignment: CENTER;");
  }

  // NB propertyName must have a corresponding method xxxProperty() in T
  protected void addColumnNumber (String heading, int width, String propertyName)
  {
    TreeTableColumn<T, Number> column = new TreeTableColumn<> (heading);
    column.setPrefWidth (width);
    column
        .setCellValueFactory (new TreeItemPropertyValueFactory<T, Number> (propertyName));
    getColumns ().add (column);
    column.setStyle ("-fx-alignment: CENTER-RIGHT;");
  }
}
