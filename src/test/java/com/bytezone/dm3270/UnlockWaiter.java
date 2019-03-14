package com.bytezone.dm3270;

import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnlockWaiter extends ConditionWaiter implements KeyboardStatusListener {

  private static final Logger LOG = LoggerFactory.getLogger(UnlockWaiter.class);

  private boolean isInputInhibited;

  public UnlockWaiter(long timeoutMillis, TerminalClient client, ScheduledExecutorService stableTimeoutExecutor) {
    super(timeoutMillis, client, stableTimeoutExecutor);
    client.addKeyboardStatusListener(this);
    isInputInhibited = client.isKeyboardLocked();
    if (!isInputInhibited) {
      LOG.debug("Start stable period since input is not inhibited");
      startStablePeriod();
    }
  }

  @Override
  public void keyboardStatusChanged(KeyboardStatusChangedEvent keyboardStatusChangedEvent) {
    LOG.debug("keyboardStatusChanged {}", keyboardStatusChangedEvent.toString());

    boolean wasInputInhibited = isInputInhibited;
    isInputInhibited = keyboardStatusChangedEvent.keyboardLocked;
    if (isInputInhibited != wasInputInhibited) {
      if (isInputInhibited) {
        LOG.debug("Cancel stable period since input has been inhibited");
        endStablePeriod();
      } else {
        LOG.debug("Start stable period since input is no longer inhibited");
        startStablePeriod();
      }
    }
  }

  @Override
  protected void stop() {
    super.stop();
    client.removeKeyboardStatusListener(this);
  }

}