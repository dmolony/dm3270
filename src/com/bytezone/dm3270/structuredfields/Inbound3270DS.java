package com.bytezone.dm3270.structuredfields;

class Inbound3270DS extends StructuredField
{
  // ReadPartitionSF provides this functionality

  private Inbound3270DS (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);

    assert data[0] == StructuredField.INBOUND_3270DS;
    System.out.printf ("Inbound 3270DS%n");     // haven't seen one yet
  }
}