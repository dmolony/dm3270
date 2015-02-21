package com.bytezone.dm3270.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.ColorAttribute;
import com.bytezone.dm3270.attributes.ForegroundColor;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.commands.ReadStructuredFieldCommand;
import com.bytezone.dm3270.commands.WriteCommand;
import com.bytezone.dm3270.commands.WriteControlCharacter;
import com.bytezone.dm3270.orders.InsertCursorOrder;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.orders.SetBufferAddressOrder;
import com.bytezone.dm3270.orders.StartFieldExtendedOrder;
import com.bytezone.dm3270.orders.StartFieldOrder;
import com.bytezone.dm3270.orders.TextOrder;
import com.bytezone.dm3270.session.Session;
import com.bytezone.dm3270.session.SessionRecord;
import com.bytezone.dm3270.session.SessionRecord.SessionRecordType;
import com.bytezone.dm3270.streams.MainframeServer;
import com.bytezone.dm3270.streams.TelnetSocket.Source;

public class MainframeStage extends BasicTelnetStage implements Mainframe
{
  private static final int WIDTH = 680;
  private static final int HEIGHT = 800;
  private static final int BUTTON_WIDTH = 105;

  private final List<Button> buttons = new ArrayList<> ();
  private final TextArea textArea = getTextArea (600);
  private MainframeServer mainframeServer;

  final RadioButton btnFieldMode;
  final RadioButton btnExtendedFieldMode;
  final RadioButton btnCharacterMode;

  final Button btnReadBuffer;// = new Button ("Read Buffer");
  final Button btnReadModified;// = new Button ("Read Modified");

  private static boolean UNPROTECTED = false;
  private static boolean PROTECTED = true;
  private static boolean ALPHA = false;
  private static boolean NUMERIC = true;

  private final List<Attribute> attributes = new ArrayList<> ();
  private final StartFieldAttribute fldOut = new StartFieldAttribute (
      StartFieldAttribute.compile (PROTECTED, ALPHA, false, false, false));
  private final StartFieldAttribute fldInAlpha = new StartFieldAttribute (
      StartFieldAttribute.compile (UNPROTECTED, ALPHA, false, false, false));
  private final StartFieldAttribute fldInNumeric = new StartFieldAttribute (
      StartFieldAttribute.compile (UNPROTECTED, NUMERIC, false, false, false));
  private final StartFieldAttribute fldEnd = new StartFieldAttribute (
      StartFieldAttribute.compile (PROTECTED, NUMERIC, true, true, false));

  public MainframeStage (int mainframePort)     // usually 5555
  {
    mainframeServer = new MainframeServer (mainframePort);
    mainframeServer.setStage (this);

    final VBox vbox = getVBox ();
    for (int i = 0; i < 10; i++)
      buttons.add (getButton ("Empty", vbox, BUTTON_WIDTH));

    final HBox hbox = getHBox ();
    btnReadBuffer = getButton ("Read Buffer", hbox, BUTTON_WIDTH);
    btnReadModified = getButton ("Read Modified", hbox, BUTTON_WIDTH);

    final ToggleGroup modeGroup = new ToggleGroup ();

    btnFieldMode = getRadioButton ("Field Mode", hbox, modeGroup);
    btnExtendedFieldMode = getRadioButton ("Extended Field Mode", hbox, modeGroup);
    btnCharacterMode = getRadioButton ("Character Mode", hbox, modeGroup);
    btnFieldMode.setSelected (true);        // match the default setting

    modeGroup.selectedToggleProperty ().addListener (new OnToggleHandler ());

    BorderPane borderPane = new BorderPane ();
    borderPane.setLeft (vbox);
    borderPane.setCenter (textArea);
    borderPane.setBottom (hbox);

    Scene scene = new Scene (borderPane, WIDTH, HEIGHT);
    setTitle ("Mainframe: " + mainframePort);
    setScene (scene);

    Rectangle2D screen = Screen.getPrimary ().getVisualBounds ();
    setX (screen.getMinX () + screen.getWidth () - WIDTH - 20);
    setY (screen.getMinY () + screen.getHeight () - HEIGHT - 40);

    prepareButtons (textArea);
  }

  public void startServer ()
  {
    new Thread (mainframeServer).start ();
  }

  private SessionRecord createCommand ()
  {
    List<Order> orders = new ArrayList<> ();

    addOutputField (orders, "Hello, World!", 170, ColorAttribute.COLOR_YELLOW);
    addInputField (orders, 184, 8, ColorAttribute.COLOR_BLUE, true);
    addOutputField (orders, "Refrigerator", 250, ColorAttribute.COLOR_YELLOW);
    addInputField (orders, 264, 8, ColorAttribute.COLOR_GREEN, true);
    addOutputField (orders, "Some numbers", 330, ColorAttribute.COLOR_YELLOW);
    addInputField (orders, 344, 8, ColorAttribute.COLOR_PINK, false);

    addInsertCursor (orders, 185);

    // create an EraseWrite command
    WriteControlCharacter wcc = new WriteControlCharacter ((byte) 0x7A);
    Command command = new WriteCommand (wcc, true, orders);

    return new SessionRecord (SessionRecordType.TN3270, command, Source.SERVER,
        LocalDateTime.now (), true);
  }

  private void
      addOutputField (List<Order> orders, String label, int position, byte color)
  {
    orders.add (new SetBufferAddressOrder (position));
    attributes.clear ();
    attributes.add (new ForegroundColor (color));
    //    orders.add (new StartFieldExtendedOrder (fldOut, attributes, label));
    orders.add (new StartFieldExtendedOrder (fldOut, attributes));
    orders.add (new TextOrder (label));
  }

  private void addInputField (List<Order> orders, int position, int length, byte color,
      boolean alpha)
  {
    orders.add (new SetBufferAddressOrder (position));
    attributes.clear ();
    attributes.add (new ForegroundColor (color));
    orders.add (new StartFieldExtendedOrder (alpha ? fldInAlpha : fldInNumeric,
        attributes));

    // end of field
    orders.add (new SetBufferAddressOrder (position + length + 1));
    orders.add (new StartFieldOrder (fldEnd));
  }

  private void addInsertCursor (List<Order> orders, int position)
  {
    orders.add (new SetBufferAddressOrder (position));
    orders.add (new InsertCursorOrder ());
  }

  private void prepareButtons (TextArea textArea)
  {
    String userHome = System.getProperty ("user.home");
    String filename = userHome + "/Dropbox/Mainframe documentation/mf.txt";
    Session session = new Session (null, filename);
    List<String> labels = session.getLables ();

    SessionRecord dr = createCommand ();
    session.add (dr);
    labels.add ("Test");

    int buttonNo = 0;
    for (SessionRecord dataRecord : session)
    {
      if (buttonNo < buttons.size ())
      {
        Button button = buttons.get (buttonNo);
        button.setOnAction ( (x) -> {
          textArea.setText (dataRecord.getMessage ().toString ());
          textArea.appendText ("\n\n");
          textArea.appendText (Utility.toHex (dataRecord.getBuffer ()));
          textArea.positionCaret (0);
          try
          {
            mainframeServer.write (dataRecord.getMessage ().getTelnetData ());
          }
          catch (Exception e)
          {
            e.printStackTrace ();
          }
        });

        if (buttonNo < labels.size ())
          button.setText (labels.get (buttonNo));

        buttonNo++;
      }
    }

    btnReadBuffer.setOnAction ( (x) -> {
      try
      {
        mainframeServer.write (createReadBufferCommand ((byte) 0xF2));   // ReadBuffer
      }
      catch (Exception e)
      {
        e.printStackTrace ();
      }
    });

    btnReadModified.setOnAction ( (x) -> {
      try
      {
        mainframeServer.write (createReadBufferCommand ((byte) 0xF6));   // ReadModified
      }
      catch (Exception e)
      {
        e.printStackTrace ();
      }
    });
  }

  @Override
  public void receiveCommand (Command command)
  {
    if (command instanceof ReadStructuredFieldCommand)
      enableButtons (true);

    textArea.setText (command.toString ());
    textArea.appendText ("\n\n");
    textArea.appendText (Utility.toHex (command.getData ()));
    textArea.positionCaret (0);
  }

  private void enableButtons (boolean enable)
  {
    for (Button button : buttons)
      button.setDisable (!enable);
    btnReadBuffer.setDisable (!enable);
    btnReadModified.setDisable (!enable);

    buttons.get (0).fire ();
    buttons.get (0).requestFocus ();
    this.requestFocus ();
  }

  public void disconnect ()
  {
    if (mainframeServer != null)
    {
      mainframeServer.close ();
      mainframeServer = null;
    }
  }

  private class OnToggleHandler implements ChangeListener<Toggle>
  {
    @Override
    public void changed (ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1)
    {
      try
      {
        if (t1 == btnFieldMode)
          mainframeServer.write (createSetReplyModeCommand ((byte) 0x00));
        else if (t1 == btnExtendedFieldMode)
          mainframeServer.write (createSetReplyModeCommand ((byte) 0x01));
        else if (t1 == btnCharacterMode)
          mainframeServer.write (createSetReplyModeCommand ((byte) 0x02));
      }
      catch (Exception e)
      {
        e.printStackTrace ();
      }
    }
  }
}