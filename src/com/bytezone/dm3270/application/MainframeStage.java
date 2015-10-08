package com.bytezone.dm3270.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainframeStage extends Stage implements Mainframe
{
  private static final int BUTTON_WIDTH = 120;

  private static boolean UNPROTECTED = false;
  private static boolean PROTECTED = true;
  private static boolean ALPHA = false;
  private static boolean NUMERIC = true;

  private final GuiFactory factory = new GuiFactory ();

  private final List<Button> buttons = new ArrayList<> ();
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

  public MainframeStage (int mainframePort)// usually 5555
  {
    mainframeServer = new MainframeServer (mainframePort);
    mainframeServer.setStage (this);

    final VBox vbox1 = factory.getVBox ();

    for (int i = 0; i < 10; i++)
      buttons.add (factory.getButton ("Empty", vbox1, BUTTON_WIDTH));

    Separator separator = new Separator ();
    separator.setOrientation (Orientation.HORIZONTAL);

    final VBox vbox2 = factory.getVBox ();
    btnReadBuffer = factory.getButton ("Read Buffer", vbox2, BUTTON_WIDTH);
    btnReadModified = factory.getButton ("Read Modified", vbox2, BUTTON_WIDTH);
    btnReadModifiedAll = factory.getButton ("Read Mod All", vbox2, BUTTON_WIDTH);
    btnEraseAllUnprotected = factory.getButton ("Erase All Unpr", vbox2, BUTTON_WIDTH);
    btnProgramTab = factory.getButton ("Program Tab", vbox2, BUTTON_WIDTH);

    final ToggleGroup modeGroup = new ToggleGroup ();

    btnFieldMode = factory.getRadioButton ("Field Mode", vbox2, modeGroup);
    btnExtendedFieldMode =
        factory.getRadioButton ("Extended Field Mode", vbox2, modeGroup);
    btnCharacterMode = factory.getRadioButton ("Character Mode", vbox2, modeGroup);
    btnFieldMode.setSelected (true);// match the default setting

    modeGroup.selectedToggleProperty ().addListener (new OnToggleHandler ());

    BorderPane borderPane = new BorderPane ();
    borderPane.setLeft (vbox1);
    borderPane.setRight (vbox2);

    Scene scene = new Scene (borderPane);
    setTitle ("Mainframe: " + mainframePort);
    setScene (scene);

    setX (1000);
    setY (100);

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

  private void addOutputField (List<Order> orders, String label, int position, byte color)
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
    InputStream in = Console.class.getClassLoader ()
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
          button.setOnAction (x -> mainframeServer
              .write (sessionRecord.getMessage ().getTelnetData ()));

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

    btnReadBuffer.setOnAction ( (x) -> mainframeServer
        .write (commandFactory.createReadBufferCommand (Command.READ_BUFFER_F2)));

    btnReadModified.setOnAction ( (x) -> mainframeServer
        .write (commandFactory.createReadBufferCommand (Command.READ_MODIFIED_F6)));

    btnReadModifiedAll.setOnAction ( (x) -> mainframeServer
        .write (commandFactory.createReadBufferCommand (Command.READ_MODIFIED_ALL_6E)));

    btnEraseAllUnprotected.setOnAction ( (x) -> mainframeServer.write (commandFactory
        .createReadBufferCommand (Command.ERASE_ALL_UNPROTECTED_6F)));

    btnProgramTab.setOnAction ( (x) -> mainframeServer
        .write (commandFactory.createProgramTabCommand ()));
  }

  @Override
  public void receiveCommand (Command command)
  {
    if (command instanceof ReadStructuredFieldCommand)
      enableButtons (true);

    if (command instanceof AIDCommand && ((AIDCommand) command).isPAKey ())
    {
      mainframeServer.write (commandFactory.createSetReplyModeCommand ((byte) 2));
      mainframeServer
          .write (commandFactory.createReadBufferCommand (Command.READ_MODIFIED_ALL_6E));
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

    btnFieldMode.setDisable (!enable);
    btnExtendedFieldMode.setDisable (!enable);
    btnCharacterMode.setDisable (!enable);

    toFront ();
    buttons.get (3).fire ();          // ISPF (Erase Write)
    buttons.get (4).fire ();          // 3.4  (Write)
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
      if (t1 == btnFieldMode)
        mainframeServer.write (commandFactory.createSetReplyModeCommand ((byte) 0x00));
      else if (t1 == btnExtendedFieldMode)
        mainframeServer.write (commandFactory.createSetReplyModeCommand ((byte) 0x01));
      else if (t1 == btnCharacterMode)
        mainframeServer.write (commandFactory.createSetReplyModeCommand ((byte) 0x02));
    }
  }
}