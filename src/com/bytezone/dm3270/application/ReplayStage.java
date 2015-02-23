package com.bytezone.dm3270.application;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Screen;

import com.bytezone.dm3270.session.Session;
import com.bytezone.dm3270.session.SessionRecord;
import com.bytezone.dm3270.session.SessionRecord.SessionRecordType;
import com.bytezone.dm3270.session.SessionTable;

public class ReplayStage extends BasicStage
{
  Console console;
  private final SessionTable table = new SessionTable ();

  public ReplayStage (ScreenHandler screenHandler, String filename, Console console)
  {
    this.console = console;

    Session session = new Session (screenHandler, filename);

    final Label label =
        new Label (session.getClientName () + " : " + session.getServerName ());
    label.setFont (new Font ("Arial", 20));
    label.setPadding (new Insets (10, 10, 10, 10));    // trbl

    final CheckBox showTelnet = new CheckBox ("Show telnet");
    final CheckBox show3270E = new CheckBox ("Show 3270-E");

    final HBox hbox = getHBox ();
    hbox.getChildren ().addAll (showTelnet, show3270E);

    final TextArea textArea = getTextArea (600);
    final TextArea replyTextArea = getTextArea (600);

    SplitPane splitPane1 = new SplitPane ();
    SplitPane splitPane2 = new SplitPane ();
    splitPane1.setOrientation (Orientation.HORIZONTAL);
    splitPane2.setOrientation (Orientation.VERTICAL);

    final StackPane sp1 = new StackPane ();
    sp1.getChildren ().add (table);

    final StackPane sp2 = new StackPane ();
    sp2.getChildren ().add (textArea);

    final StackPane sp3 = new StackPane ();
    sp3.getChildren ().add (replyTextArea);

    splitPane1.getItems ().addAll (sp1, splitPane2);
    splitPane1.setDividerPositions (0.35f);

    splitPane2.getItems ().addAll (sp2, sp3);
    splitPane2.setDividerPositions (0.75f);

    BorderPane borderPane = new BorderPane ();
    borderPane.setCenter (splitPane1);
    borderPane.setTop (label);
    borderPane.setBottom (hbox);

    setTitle ("Replay Commands - " + filename);

    Scene scene = new Scene (borderPane);
    setScene (scene);

    ObservableList<SessionRecord> masterData = session.getDataRecords ();
    FilteredList<SessionRecord> filteredData = new FilteredList<> (masterData, p -> true);
    SortedList<SessionRecord> sortedData = new SortedList<> (filteredData);
    sortedData.comparatorProperty ().bind (table.comparatorProperty ());
    table.setItems (sortedData);

    ChangeListener<? super Boolean> changeListener1 =
        ( (observable, oldValue, newValue) -> {

          // get the previously selected line
          SessionRecord selectedRecord = table.getSelectionModel ().getSelectedItem ();

          filteredData.setPredicate (dataRecord -> {

            boolean isTelnet =
                dataRecord.getDataRecordType () == SessionRecordType.TELNET;
            if (!showTelnet.isSelected () && isTelnet)
              return false;

            boolean isTN3270Ext =
                dataRecord.getDataRecordType () == SessionRecordType.TN3270E;
            if (!show3270E.isSelected () && isTN3270Ext)
              return false;

            return true;      // show the record
            });

          // restore the previously selected item (if it is still visible)
          if (selectedRecord != null)
          {
            table.getSelectionModel ().select (selectedRecord);
            table.requestFocus ();
          }
        });

    showTelnet.selectedProperty ().addListener (changeListener1);
    show3270E.selectedProperty ().addListener (changeListener1);

    showTelnet.setSelected (true);      // must be a bug
    //    showTelnet.setSelected (false);
    show3270E.setSelected (true);

    table
        .getSelectionModel ()
        .selectedItemProperty ()
        .addListener ( (ObservableValue<? extends SessionRecord> observable,
                          SessionRecord oldValue, SessionRecord newValue) //
                      -> replay (newValue, textArea, replyTextArea, DO_PROCESS));

    Rectangle2D primaryScreenBounds = Screen.getPrimary ().getVisualBounds ();
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
  }
}