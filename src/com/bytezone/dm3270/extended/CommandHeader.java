package com.bytezone.dm3270.extended;

import com.bytezone.dm3270.buffers.AbstractReplyBuffer;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.utilities.Utility;

public class CommandHeader extends AbstractReplyBuffer
{
  private static final byte TN3270_DATA = 0x00;
  private static final byte SCS_DATA = 0x01;
  private static final byte RESPONSE = 0x02;
  private static final byte BIND_IMAGE = 0x03;
  private static final byte UNBIND = 0x04;
  private static final byte NVT_DATA = 0x05;
  private static final byte REQUEST = 0x06;
  private static final byte SSCP_LU_DATA = 0x07;
  private static final byte PRINT_EOJ = 0x08;

  private static final byte ERR_COND_CLEARED = 0x00;

  private static final byte RQ_NO_RESPONSE = 0x00;
  private static final byte RQ_ERROR_RESPONSE = 0x01;
  private static final byte RQ_ALWAYS_RESPONSE = 0x02;

  private static final byte POSITIVE_RESPONSE = 0x00;
  private static final byte NEGATIVE_RESPONSE = 0x01;

  private DataType dataType;
  private ResponseType responseType;
  private RequestType requestType;
  private final int commandSeq;

  public enum DataType
  {
    TN3270_DATA, SCS_DATA, RESPONSE, BIND_IMAGE, UNBIND, NVT_DATA, REQUEST, SSCP_LU_DATA,
    PRINT_EOJ
  }

  public enum RequestType
  {
    ERR_COND_CLEARED
  }

  public enum ResponseType
  {
    NO_RESPONSE, ERROR_RESPONSE, ALWAYS_RESPONSE, POSITIVE_RESPONSE, NEGATIVE_RESPONSE
  }

  public CommandHeader (byte[] buffer)
  {
    this (buffer, 0, buffer.length);
    assert buffer.length == 5;
  }

  public CommandHeader (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);

    switch (buffer[0])
    {
      case TN3270_DATA:
        dataType = DataType.TN3270_DATA;
        break;
      case SCS_DATA:
        dataType = DataType.SCS_DATA;
        break;
      case RESPONSE:
        dataType = DataType.RESPONSE;
        break;
      case BIND_IMAGE:
        dataType = DataType.BIND_IMAGE;
        break;
      case UNBIND:
        dataType = DataType.UNBIND;
        break;
      case NVT_DATA:
        dataType = DataType.NVT_DATA;
        break;
      case REQUEST:
        dataType = DataType.REQUEST;
        break;
      case SSCP_LU_DATA:
        dataType = DataType.SSCP_LU_DATA;
        break;
      case PRINT_EOJ:
        dataType = DataType.PRINT_EOJ;
        break;
      default:
        System.out.printf ("Unknown data type: %02X%n", buffer[0]);
    }

    if (dataType == DataType.REQUEST)
    {
      if (buffer[2] == ERR_COND_CLEARED)
        requestType = RequestType.ERR_COND_CLEARED;
      else
        System.out.println ("Unknown datatype: " + dataType);
    }

    if (dataType == DataType.TN3270_DATA || dataType == DataType.SCS_DATA)
    {
      switch (buffer[2] & 0xFF)
      {
        case RQ_NO_RESPONSE:
          responseType = ResponseType.NO_RESPONSE;
          break;
        case RQ_ERROR_RESPONSE:
          responseType = ResponseType.ERROR_RESPONSE;
          break;
        case RQ_ALWAYS_RESPONSE:
          responseType = ResponseType.ALWAYS_RESPONSE;
          break;
      }
    }

    if (dataType == DataType.RESPONSE)
    {
      switch (buffer[2] & 0xFF)
      {
        case POSITIVE_RESPONSE:
          responseType = ResponseType.POSITIVE_RESPONSE;
          break;
        case NEGATIVE_RESPONSE:
          responseType = ResponseType.NEGATIVE_RESPONSE;
          break;
      }
    }

    commandSeq = Utility.unsignedShort (buffer, 3);
  }

  public DataType getDataType ()
  {
    return dataType;
  }

  public ResponseType getResponseType ()
  {
    return responseType;
  }

  public RequestType getRequestType ()
  {
    return requestType;
  }

  public int getSequence ()
  {
    return commandSeq;
  }

  @Override
  public void process (Screen screen)
  {
    if (responseType == ResponseType.ALWAYS_RESPONSE)
    {
      byte[] header = new byte[5];
      header[0] = 0x02;
      Utility.packUnsignedShort (commandSeq, header, 3);
      CommandHeader commandHeader = new CommandHeader (header);
      byte[] value = { 0x00 };

      reply = new ResponseCommand (commandHeader, value, 0, value.length);
    }
  }

  // A CommandHeader needs to expand possible 0xFF bytes, but it does not
  // have EOR bytes on the end.
  @Override
  public byte[] getTelnetData ()
  {
    int length = data.length + countFF (data);
    if (length == data.length)
      return data;

    byte[] returnBuffer = new byte[length];
    copyAndExpand (data, returnBuffer, 0);
    return returnBuffer;
  }

  @Override
  public String toString ()
  {
    String requestText = requestType == null ? "" : requestType.toString ();
    String responseText = responseType == null ? "" : responseType.toString ();

    return String.format ("HDR: %04d, %-12s, %s, %s", commandSeq, dataType, requestText,
                          responseText);
  }
}