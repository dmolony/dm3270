package com.bytezone.dm3270.application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import com.bytezone.dm3270.buffers.Buffer;
import com.bytezone.dm3270.buffers.ReplyBuffer;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.extended.AbstractExtendedCommand;
import com.bytezone.dm3270.extended.CommandHeader;
import com.bytezone.dm3270.session.SessionRecord;
import com.bytezone.dm3270.session.SessionRecord.SessionRecordType;
import com.bytezone.dm3270.streams.TelnetSocket.Source;

public class BasicStage extends Stage
{
  protected static final boolean DONT_PROCESS = false;
  protected static final boolean DO_PROCESS = true;

  protected HBox getHBox ()
  {
    HBox hbox = new HBox ();
    hbox.setSpacing (15);
    hbox.setPadding (new Insets (10, 10, 10, 10));    // trbl
    hbox.setAlignment (Pos.CENTER_LEFT);
    return hbox;
  }

  protected VBox getVBox ()
  {
    VBox vbox = new VBox ();
    vbox.setSpacing (15);
    vbox.setPadding (new Insets (10, 10, 10, 10));    // trbl
    return vbox;
  }

  protected Button getButton (String name, VBox vbox, int width)
  {
    Button button = new Button (name);
    button.setPrefWidth (width);
    vbox.getChildren ().add (button);
    button.setDisable (true);
    return button;
  }

  protected Button getButton (String name, HBox hbox, int width)
  {
    Button button = new Button (name);
    button.setPrefWidth (width);
    hbox.getChildren ().add (button);
    button.setDisable (true);
    return button;
  }

  protected TextArea getTextArea (int width)
  {
    TextArea textArea = new TextArea ();
    textArea.setEditable (false);
    textArea.setFont (Font.font ("Monospaced", 12));
    textArea.setPrefWidth (width);
    return textArea;
  }

  protected RadioButton getRadioButton (String text, HBox hbox, ToggleGroup group)
  {
    RadioButton button = new RadioButton (text);
    hbox.getChildren ().add (button);
    button.setToggleGroup (group);
    return button;
  }

  protected void replay (SessionRecord sessionRecord, TextArea textArea,
      TextArea replyTextArea, boolean process, Screen screen)
  {
    if (sessionRecord == null)     // nothing selected
      return;

    boolean ebcdic = false;
    CommandHeader commandHeader = null;

    if (sessionRecord.getDataRecordType () != SessionRecordType.TELNET)
    {
      ebcdic = true;
      if (sessionRecord.getMessage () instanceof AbstractExtendedCommand)
        commandHeader =
            ((AbstractExtendedCommand) sessionRecord.getMessage ()).getCommandHeader ();
    }

    ReplyBuffer message = sessionRecord.getMessage ();

    if (process)
      message.process ();       // only process the message when in Replay mode

    if (textArea != null)
    {
      textArea.setText (sessionRecord.getSourceName ());
      textArea.appendText (":\n\n");

      if (commandHeader != null)
      {
        textArea.appendText (commandHeader.toString ());
        textArea.appendText ("\n\n");
      }

      textArea.appendText (message.toString ());

      textArea.appendText ("\n\n");
      textArea.appendText (screen.getScreen ());

      textArea.appendText ("\n");
      textArea.appendText (Utility.toHex (sessionRecord.getBuffer (), ebcdic));

      if (sessionRecord.getSource () == Source.SERVER)
      {
        textArea.appendText ("\n\n");
        textArea.appendText (screen.getFieldText ());
      }
      textArea.positionCaret (0);
    }

    if (replyTextArea != null)
    {
      Buffer reply = message.getReply ();
      if (reply == null || reply.size () == 0)
        replyTextArea.setText ("");
      else
      {
        replyTextArea.setText ("Reply:");
        if (reply instanceof AbstractExtendedCommand)
        {
          CommandHeader header = ((AbstractExtendedCommand) reply).getCommandHeader ();
          if (header != null)
          {
            replyTextArea.appendText ("\n\n");
            replyTextArea.appendText (header.toString ());
            replyTextArea.appendText ("\n\n");
            replyTextArea.appendText (Utility.toHex (header.getData (), ebcdic));
          }
        }

        replyTextArea.appendText ("\n\n");
        replyTextArea.appendText (reply.toString ());
        replyTextArea.appendText ("\n\n");
        replyTextArea.appendText (Utility.toHex (reply.getTelnetData (), ebcdic));
        replyTextArea.positionCaret (0);
      }
    }
  }
}