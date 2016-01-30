package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.Attribute.AttributeType;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.orders.BufferAddress;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.structuredfields.SetReplyModeSF;

class ScreenPacker implements ScreenChangeListener
{
  private final byte[] buffer = new byte[8192];
  private final List<String> tsoCommands = new ArrayList<> ();

  private ScreenWatcher screenDetails;
  private Pen pen;
  private final FieldManager fieldManager;

  public ScreenPacker (Pen pen, FieldManager fieldManager)
  {
    this.pen = pen;
    this.fieldManager = fieldManager;
  }

  // used when the screen changes to a different terminal model in replay mode
  void setPen (Pen pen)
  {
    this.pen = pen;
  }

  public AIDCommand readModifiedFields (byte currentAID, int cursorLocation,
      boolean readModifiedAll)
  {
    // pack the AID
    int ptr = 0;
    buffer[ptr++] = currentAID;               // whatever key was pressed

    // PA keys and the CLR key only return the AID byte
    if (!readModifiedAll)
      if (currentAID == AIDCommand.AID_PA1 || currentAID == AIDCommand.AID_PA2
          || currentAID == AIDCommand.AID_PA3 || currentAID == AIDCommand.AID_CLEAR)
        return new AIDCommand (buffer, 0, ptr);

    // pack the cursor address
    BufferAddress ba = new BufferAddress (cursorLocation);
    ptr = ba.packAddress (buffer, ptr);

    Field tsoCommandField = screenDetails.getTSOCommandField ();
    boolean isTSOScreen = screenDetails.isTSOCommandScreen ();
    boolean tsoFieldSent = false;

    // pack all modified fields
    for (Field field : fieldManager.getUnprotectedFields ())
      if (field.isModified ())
      {
        ptr = packField (field, buffer, ptr);
        if (field == tsoCommandField)
        {
          addTSOCommand (field.getText ().trim ());
          tsoFieldSent = true;
        }
      }

    if (isTSOScreen && !tsoFieldSent)
    {
      String tsoCommand = tsoCommandField.getText ().trim ();
      if (!tsoCommand.isEmpty ())
        addTSOCommand (tsoCommand);
    }

    return new AIDCommand (buffer, 0, ptr);
  }

  private int packField (Field field, byte[] buffer, int ptr)
  {
    assert field.isModified ();

    for (ScreenPosition sp : field)
      if (sp.isStartField ())
      {
        buffer[ptr++] = Order.SET_BUFFER_ADDRESS;
        BufferAddress ba = new BufferAddress (field.getFirstLocation ());
        ptr = ba.packAddress (buffer, ptr);
      }
      else if (!sp.isNull ())
        buffer[ptr++] = sp.getByte ();                  // suppress nulls

    return ptr;
  }

  public AIDCommand readBuffer (byte currentAID, int cursorLocation, byte replyMode,
      byte[] replyTypes)
  {
    // pack the AID
    int ptr = 0;
    buffer[ptr++] = currentAID;

    // pack the cursor address
    BufferAddress ba = new BufferAddress (cursorLocation);
    ptr = ba.packAddress (buffer, ptr);

    // pack every screen location
    for (ScreenPosition sp : pen)
      if (sp.isStartField ())
        ptr = packStartPosition (sp, buffer, ptr, replyMode);
      else
        // don't suppress nulls
        ptr = packDataPosition (sp, buffer, ptr, replyMode, replyTypes);

    return new AIDCommand (buffer, 0, ptr);
  }

  private int packStartPosition (ScreenPosition sp, byte[] buffer, int ptr,
      byte replyMode)
  {
    assert sp.isStartField ();

    StartFieldAttribute sfa = sp.getStartFieldAttribute ();

    if (replyMode == SetReplyModeSF.RM_FIELD)
    {
      buffer[ptr++] = Order.START_FIELD;
      buffer[ptr++] = sfa.getAttributeValue ();
    }
    else
    {
      buffer[ptr++] = Order.START_FIELD_EXTENDED;

      List<Attribute> attributes = sp.getAttributes ();
      buffer[ptr++] = (byte) (attributes.size () + 1);    // +1 for StartFieldAttribute

      ptr = sfa.pack (buffer, ptr);                       // pack the SFA first
      for (Attribute attribute : attributes)
        ptr = attribute.pack (buffer, ptr);               // then pack the rest
    }

    return ptr;
  }

  private int packDataPosition (ScreenPosition sp, byte[] buffer, int ptr, byte replyMode,
      byte[] replyTypes)
  {
    if (replyMode == SetReplyModeSF.RM_CHARACTER)
      for (Attribute attribute : sp.getAttributes ())
        if (attribute.getAttributeType () == AttributeType.RESET)
        {
          buffer[ptr++] = Order.SET_ATTRIBUTE;
          ptr = attribute.pack (buffer, ptr);
        }
        else
          for (byte b : replyTypes)
            if (attribute.matches (b))
            {
              buffer[ptr++] = Order.SET_ATTRIBUTE;
              ptr = attribute.pack (buffer, ptr);
              break;
            }

    if (sp.isGraphicsChar () && replyMode != SetReplyModeSF.RM_FIELD)
      buffer[ptr++] = Order.GRAPHICS_ESCAPE;

    buffer[ptr++] = sp.getByte ();

    return ptr;
  }

  // called from the constructor above, and also from Screen in replay mode
  void addTSOCommand (String command)
  {
    String minCommand = command.trim ().toUpperCase ();
    if (minCommand.isEmpty ())
      return;

    tsoCommands.add (minCommand);
    notifyTSOCommandListeners (minCommand);
  }

  public String getPreviousTSOCommand ()
  {
    if (tsoCommands.size () > 0)
      return tsoCommands.get (tsoCommands.size () - 1);
    return "bollocks";
  }

  public void listTSOCommands ()
  {
    System.out.println ("User commands:");
    for (String command : tsoCommands)
      System.out.printf ("[%s]%n", command);
  }

  // ---------------------------------------------------------------------------------//
  // TSOCommandListeners
  // ---------------------------------------------------------------------------------//

  private final Set<TSOCommandListener> tsoCommandListeners = new HashSet<> ();

  void notifyTSOCommandListeners (String command)
  {
    for (TSOCommandListener listener : tsoCommandListeners)
      listener.tsoCommand (command);
  }

  public void addTSOCommandListener (TSOCommandListener listener)
  {
    tsoCommandListeners.add (listener);
  }

  public void removeTSOCommandListener (TSOCommandListener listener)
  {
    tsoCommandListeners.remove (listener);
  }

  @Override
  public void screenChanged (ScreenWatcher screenDetails)
  {
    this.screenDetails = screenDetails;
  }
}