package com.bytezone.dm3270.extended;

/*
 * Logical units (LUs) are the ports through which users access the network. Type 1 and
 * type 2 peripheral node architecture support dependent LUs only. Type 2.1 peripheral
 * node architecture supports independent and dependent LUs.
 */
public class LogicalUnit {

  private final int commit;

  private final int chainingUse;
  private final int modeSelection;
  private final int responseProtocol;
  private final int scbCompression;
  private final int sendEndBracket;

  public LogicalUnit(byte value) {
    chainingUse = (value & 0x80) >> 7;
    modeSelection = (value & 0x40) >> 6;
    responseProtocol = (value & 0x30) >> 4;
    commit = (value & 0x08) >> 3;
    scbCompression = (value & 0x02) >> 1;
    sendEndBracket = (value & 0x01);
  }

  @Override
  public String toString() {
    return String.format("Chaining use ......... %02X%n", chainingUse)
        + String.format("Mode selection ....... %02X%n", modeSelection)
        + String.format("Response protocol .... %02X%n", responseProtocol)
        + String.format("Commit ............... %02X%n", commit)
        + String.format("SCB compression ...... %02X%n", scbCompression)
        + String.format("Send End Bracket ..... %02X%n", sendEndBracket);
  }

}
