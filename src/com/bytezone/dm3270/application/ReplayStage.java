package com.bytezone.dm3270.application;

import java.nio.file.Path;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import com.bytezone.dm3270.application.GuiFactory.ProcessInstruction;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.session.Session;
import com.bytezone.dm3270.session.SessionRecord;
import com.bytezone.dm3270.session.SessionRecord.SessionRecordType;
import com.bytezone.dm3270.session.SessionTable;

class ReplayStage extends Stage
{
  private final Preferences prefs;
  private final CheckBox showTelnetCB = new CheckBox ("Show telnet");
  private final CheckBox show3270ECB = new CheckBox ("Show 3270-E");

  public ReplayStage (Screen screen, Session session, Path path, Preferences prefs)
  {
    SessionTable table = new SessionTable ();
    this.prefs = prefs;

    final Label label = session.getHeaderLabel ();
    label.setFont (new Font ("Arial", 20));
    label.setPadding (new Insets (10, 10, 10, 10));    // trbl

    boolean showTelnet = prefs.getBoolean ("ShowTelnet", false);
    boolean showExtended = prefs.getBoolean ("ShowExtended", false);

    final HBox checkBoxes = new HBox ();
    checkBoxes.setSpacing (15);
    checkBoxes.getChildren ().addAll (showTelnetCB, show3270ECB);

    final VBox leftPane = getVBox ();
    leftPane.getChildren ().addAll (table, checkBoxes);

    CommandPane commandPane =
        new CommandPane (screen, table, ProcessInstruction.DoProcess);

    SplitPane splitPane = new SplitPane ();
    splitPane.setOrientation (Orientation.HORIZONTAL);

    splitPane.getItems ().addAll (leftPane, commandPane);
    splitPane.setDividerPositions (0.37f);

    BorderPane borderPane = new BorderPane ();
    borderPane.setCenter (splitPane);
    borderPane.setTop (label);

    setTitle ("Replay Commands - " + path.getFileName ());

    ObservableList<SessionRecord> masterData = session.getDataRecords ();
    FilteredList<SessionRecord> filteredData = new FilteredList<> (masterData, p -> true);

    ChangeListener<? super Boolean> changeListener =
        (observable, oldValue, newValue) -> change (table, filteredData);

    showTelnetCB.selectedProperty ().addListener (changeListener);
    show3270ECB.selectedProperty ().addListener (changeListener);

    if (true)       // this sucks - remove it when java works properly
    {
      showTelnetCB.setSelected (true);      // must be a bug
      show3270ECB.setSelected (true);
    }

    showTelnetCB.setSelected (showTelnet);
    show3270ECB.setSelected (showExtended);

    SortedList<SessionRecord> sortedData = new SortedList<> (filteredData);
    sortedData.comparatorProperty ().bind (table.comparatorProperty ());
    table.setItems (sortedData);

    Rectangle2D primaryScreenBounds =
        javafx.stage.Screen.getPrimary ().getVisualBounds ();
    String osName = System.getProperty ("os.name");
    if (osName.startsWith ("Mac"))
    {
      setX (primaryScreenBounds.getMinX () + primaryScreenBounds.getWidth ());
      setY (primaryScreenBounds.getMinY ());
    }

    // show the first displayable screen
    SessionRecord dataRecord = session.getNext (SessionRecordType.TN3270);
    if (dataRecord != null)
      table.getSelectionModel ().select (dataRecord);

    setOnCloseRequest (e -> Platform.exit ());

    Scene scene = new Scene (borderPane);
    setScene (scene);
  }

  private void change (SessionTable table, FilteredList<SessionRecord> filteredData)
  {
    // get the previously selected line
    SessionRecord selectedRecord = table.getSelectionModel ().getSelectedItem ();

    // change the filter predicate
    filteredData.setPredicate (sessionRecord -> sessionRecord.isTN3270 ()
        || (sessionRecord.isTelnet () && showTelnetCB.isSelected ())
        || (sessionRecord.isTN3270Extended () && show3270ECB.isSelected ()));

    // restore the previously selected item (if it is still visible)
    if (selectedRecord != null)
    {
      table.getSelectionModel ().select (selectedRecord);
      table.requestFocus ();
    }
  }

  protected VBox getVBox ()
  {
    VBox vbox = new VBox ();
    vbox.setSpacing (15);
    vbox.setPadding (new Insets (10, 10, 10, 10));    // trbl
    return vbox;
  }

  public void disconnect ()
  {
    prefs.putBoolean ("ShowTelnet", showTelnetCB.isSelected ());
    prefs.putBoolean ("ShowExtended", show3270ECB.isSelected ());
  }
}