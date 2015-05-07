package com.bytezone.dm3270.plugins;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

public abstract class DefaultPlugin implements Plugin
{
  protected String getMD5 (byte[] buffer)
  {
    try
    {
      byte[] digest = MessageDigest.getInstance ("MD5").digest (buffer);
      return DatatypeConverter.printHexBinary (digest);
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
    for (ScreenField field : data.screenFields)
      if (!field.isProtected)
        ++count;
    return count;
  }

  protected static List<ScreenField> getModifiableFields (PluginData data)
  {
    List<ScreenField> fields = new ArrayList<> ();
    for (ScreenField field : data.screenFields)
      if (!field.isProtected)
        fields.add (field);
    return fields;
  }

  protected static List<ScreenField> getProtectedFields (PluginData data)
  {
    List<ScreenField> fields = new ArrayList<> ();
    for (ScreenField field : data.screenFields)
      if (field.isProtected)
        fields.add (field);
    return fields;
  }

  protected static List<ScreenField> getAlphanumericFields (PluginData data)
  {
    List<ScreenField> fields = new ArrayList<> ();
    for (ScreenField field : data.screenFields)
      if (field.isAlpha)
        fields.add (field);
    return fields;
  }

  protected static List<ScreenField> getNumericFields (PluginData data)
  {
    List<ScreenField> fields = new ArrayList<> ();
    for (ScreenField field : data.screenFields)
      if (!field.isAlpha)
        fields.add (field);
    return fields;
  }
}