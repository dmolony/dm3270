package com.bytezone.dm3270.application;

import java.nio.file.Path;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.WindowEvent;

import com.bytezone.dm3270.session.Session;
import com.bytezone.dm3270.session.SessionRecord;
import com.bytezone.dm3270.session.SessionRecord.SessionRecordType;
import com.bytezone.dm3270.session.SessionTable;

public class ReplayStage extends BasicStage
{
  private static final int TEXT_WIDTH = 540;
  private final SessionTable table = new SessionTable ();

  public ReplayStage (com.bytezone.dm3270.display.Screen screen, Path path)
  {
    Session session = new Session (screen, path);

    final Label label =
        new Label (session.getClientName () + " : " + session.getServerName ());
    label.setFont (new Font ("Arial", 20));
    label.setPadding (new Insets (10, 10, 10, 10));    // trbl

    final CheckBox showTelnet = new CheckBox ("Show telnet");
    final CheckBox show3270E = new CheckBox ("Show 3270-E");

    final HBox checkBoxes = new HBox ();
    checkBoxes.setSpacing (15);
    checkBoxes.getChildren ().addAll (showTelnet, show3270E);

    final VBox leftPane = getVBox ();
    leftPane.getChildren ().addAll (table, checkBoxes);

    TabPane tabPane = new TabPane ();
    tabPane.setSide (Side.TOP);
    tabPane.setTabClosingPolicy (TabClosingPolicy.UNAVAILABLE);

    final TextArea commandTextArea = getTextArea (TEXT_WIDTH);
    final TextArea replyTextArea = getTextArea (TEXT_WIDTH);
    final TextArea screenTextArea = getTextArea (TEXT_WIDTH);
    final TextArea fieldsTextArea = getTextArea (TEXT_WIDTH);
    final TextArea bufferTextArea = getTextArea (TEXT_WIDTH);
    final TextArea replyBufferTextArea = getTextArea (TEXT_WIDTH);

    final Tab tabCommand = getTab ("Command", commandTextArea);
    final Tab tabReply = getTab ("Reply", replyTextArea);
    final Tab tabScreen = getTab ("Screen", screenTextArea);
    final Tab tabFields = getTab ("Fields", fieldsTextArea);
    final Tab tabBuffer = getTab ("Buffer", bufferTextArea);
    final Tab tabReplyBuffer = getTab ("Reply Buffer", replyBufferTextArea);

    tabPane.getTabs ().addAll (tabCommand, tabBuffer, tabFields, tabScreen, tabReply,
                               tabReplyBuffer);

    SplitPane splitPane = new SplitPane ();
    splitPane.setOrientation (Orientation.HORIZONTAL);

    splitPane.getItems ().addAll (leftPane, tabPane);
    splitPane.setDividerPositions (0.36f);

    BorderPane borderPane = new BorderPane ();
    borderPane.setCenter (splitPane);
    borderPane.setTop (label);

    setTitle ("Replay Commands - " + path.getFileName ());

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
    showTelnet.setSelected (false);
    //    show3270E.setSelected (true);

    table
        .getSelectionModel ()
        .selectedItemProperty ()
        .addListener ( (ObservableValue<? extends SessionRecord> observable,
                          SessionRecord oldValue, SessionRecord newValue) //
                      -> replay (newValue, commandTextArea, bufferTextArea,
                                 replyTextArea, replyBufferTextArea, fieldsTextArea,
                                 screenTextArea, DO_PROCESS, screen));

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

    setOnCloseRequest (new EventHandler<WindowEvent> ()
    {
      @Override
      public void handle (WindowEvent we)
      {
        Platform.exit ();
      }
    });
  }
}