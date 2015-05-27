package com.bytezone.dm3270.filetransfer;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import com.bytezone.dm3270.application.WindowSaver;

public class FileStage extends Stage
{
  private final TabPane tabPane = new TabPane ();
  private final List<Transfer> transfers = new ArrayList<> ();
  private final Preferences prefs;
  private final WindowSaver windowSaver;

  public FileStage (Preferences prefs)
  {
    this.prefs = prefs;
    windowSaver = new WindowSaver (prefs, this, "FileTransferStage");

    tabPane.setSide (Side.TOP);
    tabPane.setTabClosingPolicy (TabClosingPolicy.UNAVAILABLE);
    tabPane.setPrefSize (500, 500);           // width, height

    BorderPane borderPane = new BorderPane ();
    borderPane.setCenter (tabPane);

    Scene scene = new Scene (borderPane);
    setScene (scene);

    if (!windowSaver.restoreWindow ())
      centerOnScreen ();

    setOnCloseRequest (e -> closeWindow ());
  }

  public void add (Transfer transfer)
  {
    transfers.add (transfer);

    Tab tab = new Tab ();
    tab.setText ("#" + transfers.size ());

    TextArea textArea = new TextArea ();
    textArea.setText (transfer.toString ());
    tab.setContent (textArea);

    tabPane.getTabs ().add (tab);

    if (!isShowing ())
      show ();
  }

  private void closeWindow ()
  {
    windowSaver.saveWindow ();
  }
}