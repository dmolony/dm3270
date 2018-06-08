package com.bytezone.dm3270;

public interface ExceptionHandler {

  void onException(Exception ex);

  void onConnectionClosed();

}
