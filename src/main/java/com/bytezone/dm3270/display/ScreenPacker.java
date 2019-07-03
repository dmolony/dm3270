package com.bytezone.dm3270.display;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.extended.SscpLuDataCommand;
import com.bytezone.dm3270.orders.BufferAddress;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.structuredfields.SetReplyModeSF;

import java.util.List;

public class ScreenPacker {

  private final byte[] buffer = new byte[8192];

  private Pen pen;
  private final FieldManager fieldManager;

  public ScreenPacker(Pen pen, FieldManager fieldManager) {
    this.pen = pen;
    this.fieldManager = fieldManager;
  }

  public Command readModifiedFields(byte currentAID, int cursorLocation,
      boolean readModifiedAll, boolean sscpLuData) {
    // pack the AID
    int ptr = 0;
    if (!sscpLuData) {
      buffer[ptr++] = currentAID;               // whatever key was pressed
    }

    // PA keys and the CLR key only return the AID byte
    if (!readModifiedAll) {
      if (currentAID == AIDCommand.AID_PA1 || currentAID == AIDCommand.AID_PA2
          || currentAID == AIDCommand.AID_PA3 || currentAID == AIDCommand.AID_CLEAR) {
        return new AIDCommand(buffer, 0, ptr);
      }
    }

    if (!sscpLuData) {
      // pack the cursor address
      BufferAddress ba = new BufferAddress(cursorLocation);
      ptr = ba.packAddress(buffer, ptr);
    }

    if (!fieldManager.getFields().isEmpty()) {
      // pack all modified fields
      for (Field field : fieldManager.getFields()) {
        if (field.isModified()) {
          ptr = packField(field, buffer, ptr);
        }
      }
    } else {
      for (ScreenPosition sp : pen.fromCurrentPosition()) {
        if (!sp.isNull()) {
          buffer[ptr++] = sp.getByte();
        }
      }
    }

    return sscpLuData ? new SscpLuDataCommand(buffer, 0, ptr) : new AIDCommand(buffer, 0, ptr);
  }

  private int packField(Field field, byte[] buffer, int ptr) {
    assert field.isModified();

    for (ScreenPosition sp : field) {
      if (sp.isStartField()) {
        buffer[ptr++] = Order.SET_BUFFER_ADDRESS;
        BufferAddress ba = new BufferAddress(field.getFirstLocation());
        ptr = ba.packAddress(buffer, ptr);
      } else if (!sp.isNull()) {
        buffer[ptr++] = sp.getByte();                  // suppress nulls
      }
    }

    return ptr;
  }

  public AIDCommand readBuffer(byte currentAID, int cursorLocation, byte replyMode,
      byte[] replyTypes) {
    // pack the AID
    int ptr = 0;
    buffer[ptr++] = currentAID;

    // pack the cursor address
    BufferAddress ba = new BufferAddress(cursorLocation);
    ptr = ba.packAddress(buffer, ptr);

    // pack every screen location
    for (ScreenPosition sp : pen) {
      if (sp.isStartField()) {
        ptr = packStartPosition(sp, buffer, ptr, replyMode);
        // don't suppress nulls
      } else {
        ptr = packDataPosition(sp, buffer, ptr, replyMode, replyTypes);
      }
    }

    return new AIDCommand(buffer, 0, ptr);
  }

  private int packStartPosition(ScreenPosition sp, byte[] buffer, int ptr,
      byte replyMode) {
    assert sp.isStartField();

    StartFieldAttribute sfa = sp.getStartFieldAttribute();

    if (replyMode == SetReplyModeSF.RM_FIELD) {
      buffer[ptr++] = Order.START_FIELD;
      buffer[ptr++] = sfa.getAttributeValue();
    } else {
      buffer[ptr++] = Order.START_FIELD_EXTENDED;

      List<Attribute> attributes = sp.getAttributes();
      buffer[ptr++] = (byte) (attributes.size() + 1);    // +1 for StartFieldAttribute

      ptr = sfa.pack(buffer, ptr);                       // pack the SFA first
      for (Attribute attribute : attributes) {
        ptr = attribute.pack(buffer, ptr);               // then pack the rest
      }
    }

    return ptr;
  }

  private int packDataPosition(ScreenPosition sp, byte[] buffer, int ptr, byte replyMode,
      byte[] replyTypes) {
    if (replyMode == SetReplyModeSF.RM_CHARACTER) {
      for (Attribute attribute : sp.getAttributes()) {
        if (attribute.getAttributeType() == Attribute.AttributeType.RESET) {
          buffer[ptr++] = Order.SET_ATTRIBUTE;
          ptr = attribute.pack(buffer, ptr);
        } else {
          for (byte b : replyTypes) {
            if (attribute.matches(b)) {
              buffer[ptr++] = Order.SET_ATTRIBUTE;
              ptr = attribute.pack(buffer, ptr);
              break;
            }
          }
        }
      }
    }

    if (sp.isGraphicsChar() && replyMode != SetReplyModeSF.RM_FIELD) {
      buffer[ptr++] = Order.GRAPHICS_ESCAPE;
    }

    buffer[ptr++] = sp.getByte();

    return ptr;
  }

}
