package com.bytezone.dm3270.buffers;

import com.bytezone.dm3270.display.Screen;

public interface Buffer {

  byte[] getData();

  byte[] getTelnetData();

  int size();

  void process(Screen screen);

}
