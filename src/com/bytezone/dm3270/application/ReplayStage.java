package com.bytezone.dm3270.application;

import java.nio.file.Path;
import java.util.prefs.Preferences;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.session.Session;
import com.bytezone.dm3270.session.SessionRecord;
import com.bytezone.dm3270.session.SessionRecord.SessionRecordType;
import com.bytezone.dm3270.session.SessionTable;
import com.bytezone.dm3270.utilities.WindowSaver;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

class ReplayStage extends Stage
{
  private final Preferences prefs;
  private final CheckBox showTelnetCB = new CheckBox ("Show telnet");
  private final CheckBox show3270ECB = new CheckBox ("Show 3270-E");
  private final WindowSaver windowSaver;
  private Rectangle2D primaryScreenBounds;

  public ReplayStage (Session session, Path path, Preferences prefs, Screen screen)
  {
    this.prefs = prefs;

    final Label label = session.getHeaderLabel ();
    label.setFont (new Font ("Arial", 20));
    label.setPadding (new Insets (10, 10, 10, 10));                 // trbl

    boolean showTelnet = prefs.getBoolean ("ShowTelnet", false);
    boolean showExtended = prefs.getBoolean ("ShowExtended", false);

    final HBox checkBoxes = new HBox ();
    checkBoxes.setSpacing (15);
    checkBoxes.setPadding (new Insets (10, 10, 10, 10));            // trbl
    checkBoxes.getChildren ().addAll (showTelnetCB, show3270ECB);

    SessionTable sessionTable = new SessionTable ();
    CommandPane commandPane =
        new CommandPane (sessionTable, CommandPane.ProcessInstruction.DoProcess);

    //    commandPane.setScreen (session.getScreen ());
    commandPane.setScreen (screen);

    setTitle ("Replay Commands - " + path.getFileName ());

    ObservableList<SessionRecord> masterData = session.getDataRecords ();
    FilteredList<SessionRecord> filteredData = new FilteredList<> (masterData, p -> true);

    ChangeListener<? super Boolean> changeListener =
        (observable, oldValue, newValue) -> change (sessionTable, filteredData);

    showTelnetCB.selectedProperty ().addListener (changeListener);
    show3270ECB.selectedProperty ().addListener (changeListener);

    if (true)         // this sucks - remove it when java works properly
    {
      showTelnetCB.setSelected (true);          // must be a bug
      show3270ECB.setSelected (true);
    }

    showTelnetCB.setSelected (showTelnet);
    show3270ECB.setSelected (showExtended);

    SortedList<SessionRecord> sortedData = new SortedList<> (filteredData);
    sortedData.comparatorProperty ().bind (sessionTable.comparatorProperty ());
    sessionTable.setItems (sortedData);

    displayFirstScreen (session, sessionTable);

    setOnCloseRequest (e -> Platform.exit ());

    windowSaver = new WindowSaver (prefs, this, "Replay");
    if (!windowSaver.restoreWindow ())
    {
      primaryScreenBounds = javafx.stage.Screen.getPrimary ().getVisualBounds ();
      setX (800);
      setY (primaryScreenBounds.getMinY ());
      double height = primaryScreenBounds.getHeight ();
      setHeight (Math.min (height, 1200));
    }

    BorderPane borderPane = new BorderPane ();
    borderPane.setLeft (sessionTable);      // fixed size
    borderPane.setCenter (commandPane);     // expands to fill window
    borderPane.setTop (label);
    borderPane.setBottom (checkBoxes);

    Scene scene = new Scene (borderPane);
    setScene (scene);
  }

  private void displayFirstScreen (Session session, SessionTable table)
  {
    // look for the first useful screen
    int[] screenSizes = { 2306, 2301, 2206, 1957, 2309, 3194, 1372 };
    SessionRecord dataRecord = null;

    if (true)
      for (int screenSize : screenSizes)
      {
        dataRecord = session.getBySize (screenSize);
        if (dataRecord != null)
          break;
      }

    if (dataRecord == null)
      dataRecord = session.getNext (SessionRecordType.TN3270);

    if (dataRecord == null)
    {
      System.out.println ("No suitable first screen found");
      return;
    }

    table.getSelectionModel ().select (dataRecord);
    int index = table.getSelectionModel ().getSelectedIndex ();
    table.scrollTo (index);
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
    vbox.setPadding (new Insets (10, 10, 10, 10));              // trbl
    return vbox;
  }

  public void disconnect ()
  {
    prefs.putBoolean ("ShowTelnet", showTelnetCB.isSelected ());
    prefs.putBoolean ("ShowExtended", show3270ECB.isSelected ());
    windowSaver.saveWindow ();
  }
}