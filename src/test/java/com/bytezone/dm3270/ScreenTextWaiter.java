package com.bytezone.dm3270;

import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import com.bytezone.dm3270.display.CursorMoveListener;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenWatcher;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenTextWaiter extends ConditionWaiter implements KeyboardStatusListener,
    CursorMoveListener, ScreenChangeListener {

  private static final Logger LOG = LoggerFactory.getLogger(ScreenTextWaiter.class);

  private final String text;
  private boolean matched;

  public ScreenTextWaiter(String text, TerminalClient client, ScheduledExecutorService stableTimeoutExecutor) {
    super(client, stableTimeoutExecutor);
    this.text = text;
    client.addCursorMoveListener(this);
    client.addKeyboardStatusListener(this);
    client.addScreenChangeListener(this);
    checkIfScreenMatchesCondition();
    if (matched) {
      startStablePeriod();
    }
  }

  @Override
  public void keyboardStatusChanged(KeyboardStatusChangedEvent keyboardStatusChangedEvent) {
    handleReceivedEvent("keyboardStatusChanged");
  }

  @Override
  public void cursorMoved(int i, int i1, Field field) {
    handleReceivedEvent("cursorMoved");
  }

  @Override
  public void screenChanged(ScreenWatcher screenWatcher) {
    checkIfScreenMatchesCondition();
    handleReceivedEvent("screenChanged");
  }

  private void handleReceivedEvent(String event) {
    if (matched) {
      LOG.debug("Restart screen text stable period since received event {}", event);
      startStablePeriod();
    }
  }

  private void checkIfScreenMatchesCondition() {
    if (client.getScreenText().contains(text)) {
      LOG.debug("Found matching text in screen, now waiting for silent period.");
      matched = true;
    }
  }

  protected void stop() {
    super.stop();
    client.removeCursorMoveListener(this);
    client.removeKeyboardStatusListener(this);
    client.removeScreenChangeListener(this);
  }

}