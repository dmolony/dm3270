package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.filetransfer.TransferManager.TransferStatus;

public interface TransferListener
{
  public void transferStatusChanged (TransferStatus status, Transfer transfer);
}