package com.bytezone.dm3270.application;

import java.util.Optional;

import com.bytezone.dm3270.buffers.Buffer;
import com.bytezone.dm3270.buffers.ReplyBuffer;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.extended.AbstractExtendedCommand;
import com.bytezone.dm3270.extended.CommandHeader;
import com.bytezone.dm3270.session.SessionRecord;
import com.bytezone.dm3270.session.SessionRecord.SessionRecordType;
import com.bytezone.dm3270.session.SessionTable;
import com.bytezone.dm3270.streams.TelnetSocket.Source;
import com.bytezone.dm3270.utilities.Dm3270Utility;

import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;

class CommandPane extends TabPane
{
  private static final int TEXT_WIDTH = 20;

  private final TextArea commandTextArea = getTextArea ();
  private final TextArea replyTextArea = getTextArea ();
  private final TextArea screenTextArea = getTextArea ();
  private final TextArea fieldsTextArea = getTextArea ();
  private final TextArea bufferTextArea = getTextArea ();
  private final TextArea replyBufferTextArea = getTextArea ();

  private final ProcessInstruction processInstruction;
  private Screen screen;

  enum ProcessInstruction
  {
    DoProcess, DontProcess
  }

  public CommandPane (SessionTable sessionTable, ProcessInstruction processInstruction)
  {
    setSide (Side.TOP);
    setTabClosingPolicy (TabClosingPolicy.UNAVAILABLE);

    final Tab tabCommand = getTab ("Command", commandTextArea);
    final Tab tabReply = getTab ("Reply", replyTextArea);
    final Tab tabScreen = getTab ("Screen", screenTextArea);
    final Tab tabFields = getTab ("Fields", fieldsTextArea);
    final Tab tabBuffer = getTab ("Buffer", bufferTextArea);
    final Tab tabReplyBuffer = getTab ("Reply Buffer", replyBufferTextArea);

    this.processInstruction = processInstruction;

    getTabs ().addAll (tabCommand, tabBuffer, tabFields, tabScreen, tabReply,
                       tabReplyBuffer);

    sessionTable.getSelectionModel ().selectedItemProperty ()
        .addListener ( (observable, oldValue, newValue) -> replay (newValue));
  }

  public void setScreen (Screen screen)
  {
    this.screen = screen;
  }

  protected void replay (SessionRecord sessionRecord)
  {
    if (sessionRecord == null)            // nothing selected
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

    if (processInstruction == ProcessInstruction.DoProcess)
      message.process (screen);       // only process the message when in Replay mode

    commandTextArea.setText ("");

    if (commandHeader != null)
    {
      commandTextArea.appendText (commandHeader.toString ());
      commandTextArea.appendText ("\n\n");
    }

    commandTextArea.appendText (message.toString ());
    commandTextArea.positionCaret (0);

    bufferTextArea.setText (Dm3270Utility.toHex (sessionRecord.getBuffer (), ebcdic));
    bufferTextArea.positionCaret (0);

    // this needs to deal with a FieldManager - it needs the commands to be
    // processed, but without the overhead of the graphics display
    if (screen != null)
    {
      if (sessionRecord.getSource () == Source.SERVER)
      {
        fieldsTextArea.setText (screen.getFieldManager ().getFieldsText ());
        fieldsTextArea.positionCaret (0);
      }

      screenTextArea.setText (screen.getScreenText ());
      screenTextArea.positionCaret (0);
    }

    replyTextArea.setText ("");
    replyBufferTextArea.setText ("");

    Optional<Buffer> opt = message.getReply ();
    if (opt.isPresent ())
    {
      Buffer reply = opt.get ();
      assert reply.size () > 0;

      //      if (reply instanceof MultiBuffer)
      //      {
      //        int buffers = ((MultiBuffer) reply).totalBuffers ();
      //        for (int i = 0; i < buffers; i++)
      //        {
      //          appendCommand (replyTextArea, ((MultiBuffer) reply).getBuffer (i));
      //          replyTextArea.appendText ("\n\n");
      //        }
      //        replyTextArea.deleteText (replyTextArea.getLength () - 2,
      //                                  replyTextArea.getLength ());
      //      }
      //      else
      //      appendCommand (replyTextArea, reply);
      if (reply instanceof AbstractExtendedCommand)
      {
        CommandHeader header = ((AbstractExtendedCommand) reply).getCommandHeader ();
        if (header != null)
        {
          replyTextArea.appendText (header.toString ());
          replyTextArea.appendText ("\n\n");
        }
      }

      replyTextArea.appendText (reply.toString ());
      replyTextArea.positionCaret (0);

      replyBufferTextArea.setText (Dm3270Utility.toHex (reply.getTelnetData (), ebcdic));
      replyBufferTextArea.positionCaret (0);
    }
  }

  //  private void appendCommand (TextArea textArea, Buffer buffer)
  //  {
  //    if (buffer instanceof AbstractExtendedCommand)
  //    {
  //      CommandHeader header = ((AbstractExtendedCommand) buffer).getCommandHeader ();
  //      if (header != null)
  //      {
  //        textArea.appendText (header.toString ());
  //        textArea.appendText ("\n\n");
  //      }
  //    }
  //    textArea.appendText (buffer.toString ());
  //  }

  private Tab getTab (String name, TextArea textArea)
  {
    Tab tab = new Tab ();
    tab.setText (name);
    tab.setContent (textArea);
    return tab;
  }

  private TextArea getTextArea ()
  {
    TextArea textArea = new TextArea ();
    textArea.setEditable (false);
    textArea.setFont (Font.font ("Monospaced", 12));
    textArea.setPrefWidth (TEXT_WIDTH);
    return textArea;
  }
}