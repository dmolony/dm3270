package com.bytezone.dm3270.plugins;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public abstract class DefaultPlugin implements Plugin
{
  protected String getMD5 (byte[] buffer)
  {
    try
    {
      byte[] digest = MessageDigest.getInstance ("MD5").digest (buffer);
      //      return DatatypeConverter.printHexBinary (digest);
      return toHex (digest);
    }
    catch (NoSuchAlgorithmException e)
    {
      e.printStackTrace ();
    }
    return "";
  }

  //  protected static ScreenField getCursorField (PluginData data)
  //  {
  //    for (ScreenField field : data.screenFields)
  //    {
  //      if (field.contains (data.cursorRow, data.cursorColumn))
  //        return field;
  //    }
  //    return null;
  //  }

  protected static int countModifiableFields (PluginData data)
  {
    int count = 0;
    for (PluginField field : data.screenFields)
      if (!field.isProtected)
        ++count;
    return count;
  }

  protected static List<PluginField> getModifiableFields (PluginData data)
  {
    List<PluginField> fields = new ArrayList<> ();
    for (PluginField field : data.screenFields)
      if (!field.isProtected)
        fields.add (field);
    return fields;
  }

  protected static List<PluginField> getProtectedFields (PluginData data)
  {
    List<PluginField> fields = new ArrayList<> ();
    for (PluginField field : data.screenFields)
      if (field.isProtected)
        fields.add (field);
    return fields;
  }

  protected static List<PluginField> getAlphanumericFields (PluginData data)
  {
    List<PluginField> fields = new ArrayList<> ();
    for (PluginField field : data.screenFields)
      if (field.isAlpha)
        fields.add (field);
    return fields;
  }

  protected static List<PluginField> getNumericFields (PluginData data)
  {
    List<PluginField> fields = new ArrayList<> ();
    for (PluginField field : data.screenFields)
      if (!field.isAlpha)
        fields.add (field);
    return fields;
  }

  // ---------------------------------------------------------------------------------//
  // toHex
  // ---------------------------------------------------------------------------------//

  static String toHex (byte[] bytes)
  {
    StringBuilder builder = new StringBuilder (bytes.length * 2);
    for (int i = 0; i < bytes.length; i++)
    {
      int digit = (bytes[i] >> 4) & 0xF;
      builder.append (digit < 10 ? (char) ('0' + digit) : (char) ('A' - 10 + digit));
      digit = (bytes[i] & 0xF);
      builder.append (digit < 10 ? (char) ('0' + digit) : (char) ('A' - 10 + digit));
    }
    return builder.toString ();
  }
}