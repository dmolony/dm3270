package com.bytezone.dm3270.filetransfer;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.application.WindowSaver;

public class FileStage extends Stage
{
  private final TabPane tabPane = new TabPane ();
  private final List<Transfer> transfers = new ArrayList<> ();
  private final Preferences prefs;
  private final WindowSaver windowSaver;
  private final Button hideButton = new Button ("Hide Window");

  public FileStage (Preferences prefs)
  {
    this.prefs = prefs;
    windowSaver = new WindowSaver (prefs, this, "FileTransferStage");

    tabPane.setSide (Side.TOP);
    tabPane.setTabClosingPolicy (TabClosingPolicy.UNAVAILABLE);
    tabPane.setPrefSize (500, 500);           // width, height

    HBox buttonBox = new HBox ();
    hideButton.setPrefWidth (150);
    buttonBox.setAlignment (Pos.CENTER_RIGHT);
    buttonBox.setPadding (new Insets (10, 10, 10, 10));         // trbl

    hideButton.setOnAction (e -> {
      closeWindow ();
      hide ();
    });
    buttonBox.getChildren ().add (hideButton);

    BorderPane borderPane = new BorderPane ();
    borderPane.setCenter (tabPane);
    borderPane.setBottom (buttonBox);

    Scene scene = new Scene (borderPane);
    setScene (scene);

    if (!windowSaver.restoreWindow ())
      centerOnScreen ();

    setOnCloseRequest (e -> closeWindow ());
  }

  public void addTransfer (Transfer transfer)
  {
    transfers.add (transfer);

    Tab tab = new Tab ();
    tab.setText ("#" + transfers.size ());

    TextArea textArea = new TextArea ();
    textArea.setEditable (false);
    textArea.setFont (Font.font ("Monospaced", 12));

    if (true)
    {
      LinePrinter linePrinter = new LinePrinter (132);
      if (transfer.isData ())
      {
        byte[] fullBuffer = transfer.getAllDataBuffers ();
        linePrinter.printBuffer (fullBuffer, 132);
        textArea.appendText ("\n");
        textArea.appendText (linePrinter.getOutput ());
      }
      else
        for (DataHeader dataHeader : transfer.messageBuffers)
        {
          textArea.appendText ("\n");
          textArea.appendText (Utility.toHex (dataHeader.getBuffer (), false));
        }
    }
    else if (false)
    {
      textArea.setText (transfer.toString ());
      textArea.appendText ("\n");

      if (transfer.isData ())
        for (DataHeader dataHeader : transfer.dataBuffers)
        {
          textArea.appendText ("\n");
          textArea.appendText (Utility.toHex (dataHeader.getBuffer ()));
        }
      else
        for (DataHeader dataHeader : transfer.messageBuffers)
        {
          textArea.appendText ("\n");
          textArea.appendText (Utility.toHex (dataHeader.getBuffer (), false));
        }
    }
    else
    {
      if (transfer.isData ())
      {
        StringBuilder text = new StringBuilder ();
        int lineLength = 132;

        try
        {
          String remainder = "";
          for (DataHeader dataHeader : transfer.dataBuffers)
          {
            byte[] buffer = dataHeader.getBuffer ();
            int bytesLeft = buffer.length;
            int ptr = 0;
            int reclen = lineLength - remainder.length ();

            while (bytesLeft >= reclen)
            {
              text.append (new String (buffer, ptr, reclen, "CP1047"));
              text.append ('\n');
              ptr += reclen;
              bytesLeft -= reclen;
              reclen = lineLength;
            }
            if (bytesLeft > 0)
            {
              remainder = new String (buffer, ptr, bytesLeft, "CP1047");
              text.append (remainder);
            }
          }
        }
        catch (UnsupportedEncodingException e)
        {
          e.printStackTrace ();
        }

        if (text.charAt (text.length () - 1) == '\n')
          text.deleteCharAt (text.length () - 1);

        textArea.setText (text.toString ());
      }
      else
      {
        String message = new String (transfer.messageBuffers.get (0).getBuffer ());
        textArea.setText (message);
      }
    }

    tab.setContent (textArea);
    textArea.positionCaret (0);

    Platform.runLater ( () -> {
      tabPane.getTabs ().add (tab);
      if (!isShowing ())
        show ();
    });
  }

  private void closeWindow ()
  {
    windowSaver.saveWindow ();
  }
}