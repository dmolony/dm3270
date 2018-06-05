package com.bytezone.dm3270.extended;

public class UnbindCommand extends AbstractExtendedCommand {

  public UnbindCommand(CommandHeader commandHeader, byte[] buffer, int offset,
      int length) {
    super(commandHeader, buffer, offset, length);
  }

  @Override
  public String getName() {
    return "Unbind";
  }

  @Override
  public String toString() {
    return String.format("UNB: %02X", data[0]);
  }

}
