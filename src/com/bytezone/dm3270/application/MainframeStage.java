package com.bytezone.dm3270.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.ColorAttribute;
import com.bytezone.dm3270.attributes.ForegroundColor;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.commands.AIDCommand;
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

public class MainframeStage extends Stage implements Mainframe
{
  private static final int WIDTH = 680;
  private static final int HEIGHT = 800;
  private static final int BUTTON_WIDTH = 120;

  private static boolean UNPROTECTED = false;
  private static boolean PROTECTED = true;
  private static boolean ALPHA = false;
  private static boolean NUMERIC = true;

  private final GuiFactory factory = new GuiFactory ();

  private final List<Button> buttons = new ArrayList<> ();
  private final TextArea textArea = factory.getTextArea (600);
  private MainframeServer mainframeServer;

  private final RadioButton btnFieldMode;
  private final RadioButton btnExtendedFieldMode;
  private final RadioButton btnCharacterMode;

  private final Button btnReadBuffer;
  private final Button btnReadModified;
  private final Button btnReadModifiedAll;
  private final Button btnEraseAllUnprotected;
  private final Button btnProgramTab;

  private final CommandFactory commandFactory = new CommandFactory ();

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

    final VBox vBox = factory.getVBox ();

    for (int i = 0; i < 10; i++)
      buttons.add (factory.getButton ("Empty", vBox, BUTTON_WIDTH));

    Separator separator = new Separator ();
    separator.setOrientation (Orientation.HORIZONTAL);
    vBox.getChildren ().add (separator);

    btnReadBuffer = factory.getButton ("Read Buffer", vBox, BUTTON_WIDTH);
    btnReadModified = factory.getButton ("Read Modified", vBox, BUTTON_WIDTH);
    btnReadModifiedAll = factory.getButton ("Read Mod All", vBox, BUTTON_WIDTH);
    btnEraseAllUnprotected = factory.getButton ("Erase All Unpr", vBox, BUTTON_WIDTH);
    btnProgramTab = factory.getButton ("Program Tab", vBox, BUTTON_WIDTH);

    final ToggleGroup modeGroup = new ToggleGroup ();

    final HBox hbox = factory.getHBox ();
    btnFieldMode = factory.getRadioButton ("Field Mode", hbox, modeGroup);
    btnExtendedFieldMode =
        factory.getRadioButton ("Extended Field Mode", hbox, modeGroup);
    btnCharacterMode = factory.getRadioButton ("Character Mode", hbox, modeGroup);
    btnFieldMode.setSelected (true);        // match the default setting

    modeGroup.selectedToggleProperty ().addListener (new OnToggleHandler ());

    BorderPane borderPane = new BorderPane ();
    borderPane.setLeft (vBox);
    borderPane.setCenter (textArea);
    borderPane.setBottom (hbox);

    Scene scene = new Scene (borderPane, WIDTH, HEIGHT);
    setTitle ("Mainframe: " + mainframePort);
    setScene (scene);

    Rectangle2D screen = Screen.getPrimary ().getVisualBounds ();
    if (screen.getWidth () > 1800)
      setX (screen.getMinX () + screen.getWidth () - WIDTH - 320);
    else
      setX (screen.getMinX () + screen.getWidth () - WIDTH - 20);

    if (screen.getHeight () > 1200)
      setY (screen.getMinY () + screen.getHeight () - HEIGHT - 140);
    else
      setY (screen.getMinY () + screen.getHeight () - HEIGHT - 40);

    prepareButtons ();

    setOnCloseRequest (e -> Platform.exit ());
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

  private void prepareButtons ()
  {
    InputStream in =
        Console.class.getClassLoader ()
            .getResourceAsStream ("com/bytezone/dm3270/application/mf.txt");
    if (in == null)
    {
      System.out.println ("mf.txt not found");
      return;
    }

    BufferedReader reader = new BufferedReader (new InputStreamReader (in));
    String line;
    List<String> lines = new ArrayList<String> ();

    try
    {
      while ((line = reader.readLine ()) != null)
        lines.add (line);
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }

    try
    {
      Session session = new Session (null, lines);
      List<String> labels = session.getLabels ();

      SessionRecord dr = createCommand ();
      session.add (dr);
      labels.add ("Test");

      int buttonNo = 0;
      for (SessionRecord sessionRecord : session)
      {
        if (buttonNo < buttons.size ())
        {
          Button button = buttons.get (buttonNo);
          button.setOnAction ( (x) -> {
            textArea.setText (sessionRecord.getMessage ().toString ());
            textArea.appendText ("\n\n");
            textArea.appendText (Utility.toHex (sessionRecord.getBuffer ()));
            textArea.positionCaret (0);
            mainframeServer.write (sessionRecord.getMessage ().getTelnetData ());
          });

          if (buttonNo < labels.size ())
            button.setText (labels.get (buttonNo));

          buttonNo++;
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }

    btnReadBuffer.setOnAction ( (x) -> {
      mainframeServer.write (commandFactory
          .createReadBufferCommand (Command.READ_BUFFER_F2));
    });

    btnReadModified.setOnAction ( (x) -> {
      mainframeServer.write (commandFactory
          .createReadBufferCommand (Command.READ_MODIFIED_F6));
    });

    btnReadModifiedAll.setOnAction ( (x) -> {
      mainframeServer.write (commandFactory
          .createReadBufferCommand (Command.READ_MODIFIED_ALL_6E));
    });

    btnEraseAllUnprotected.setOnAction ( (x) -> {
      mainframeServer.write (commandFactory
          .createReadBufferCommand (Command.ERASE_ALL_UNPROTECTED_6F));
    });

    btnProgramTab.setOnAction ( (x) -> {
      mainframeServer.write (commandFactory.createProgramTabCommand ());
    });
  }

  @Override
  public void receiveCommand (Command command)
  {
    textArea.setText (command.toString ());
    textArea.appendText ("\n\n");
    textArea.appendText (Utility.toHex (command.getData ()));
    textArea.positionCaret (0);

    if (command instanceof ReadStructuredFieldCommand)
      enableButtons (true);

    if (command instanceof AIDCommand && ((AIDCommand) command).isPAKey ())
    {
      mainframeServer.write (commandFactory.createSetReplyModeCommand ((byte) 2));
      mainframeServer.write (commandFactory
          .createReadBufferCommand (Command.READ_MODIFIED_ALL_6E));
    }
  }

  private void enableButtons (boolean enable)
  {
    for (Button button : buttons)
      button.setDisable (!enable);

    btnReadBuffer.setDisable (!enable);
    btnReadModified.setDisable (!enable);
    btnReadModifiedAll.setDisable (!enable);
    btnEraseAllUnprotected.setDisable (!enable);
    btnProgramTab.setDisable (!enable);

    toFront ();
    buttons.get (3).fire ();        // ISPF (Erase Write)
    buttons.get (4).fire ();        // 3.4  (Write)
    this.requestFocus ();
    buttons.get (4).requestFocus ();
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
          mainframeServer.write (commandFactory.createSetReplyModeCommand ((byte) 0x00));
        else if (t1 == btnExtendedFieldMode)
          mainframeServer.write (commandFactory.createSetReplyModeCommand ((byte) 0x01));
        else if (t1 == btnCharacterMode)
          mainframeServer.write (commandFactory.createSetReplyModeCommand ((byte) 0x02));
      }
      catch (Exception e)
      {
        e.printStackTrace ();
      }
    }
  }
}