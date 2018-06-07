package com.bytezone.dm3270;

import static org.assertj.core.api.Assertions.assertThat;

import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import com.bytezone.dm3270.commands.AIDCommand;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import us.abstracta.wiresham.Flow;
import us.abstracta.wiresham.VirtualTcpService;

public class Tn3270ClientTest {

  private static final long TIMEOUT_MILLIS = 10000;

  private VirtualTcpService service = new VirtualTcpService();
  private Tn3270Client client;

  @Before
  public void setup() throws IOException {
    service.setFlow(Flow.fromYml(new File(getClass().getResource("/login.yml").getFile())));
    service.start();
    client = new Tn3270Client();
    client.connect("localhost", service.getPort(), TerminalType.DEFAULT_TERMINAL_TYPE);
  }

  @After
  public void teardown() throws Exception {
    client.disconnect();
    service.stop(TIMEOUT_MILLIS);
  }

  @Test(timeout = TIMEOUT_MILLIS)
  public void shouldUnlockKeyboardAfterConnect() throws Exception {
    awaitKeyboardUnlocked();
  }

  private void awaitKeyboardUnlocked() throws InterruptedException, TimeoutException {
    new KeyboardUnlockWaiter(client).await();
  }

  private static class KeyboardUnlockWaiter implements KeyboardStatusListener {

    private final Tn3270Client client;
    private final CountDownLatch latch = new CountDownLatch(1);
    private final ScheduledExecutorService stableTimer = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture stableTimeoutTask;
    private boolean wasLocked;

    private KeyboardUnlockWaiter(Tn3270Client client) {
      this.client = client;
      wasLocked = client.isKeyboardLocked();
      if (!client.isKeyboardLocked()) {
        startStableWait();
      }
      client.addKeyboardStatusListener(this);
    }

    private synchronized void startStableWait() {
      endStableWait();
      stableTimeoutTask = stableTimer.schedule(latch::countDown, 1, TimeUnit.SECONDS);
    }

    private synchronized void endStableWait() {
      if (stableTimeoutTask != null) {
        stableTimeoutTask.cancel(false);
      }
    }

    @Override
    public void keyboardStatusChanged(KeyboardStatusChangedEvent event) {
      if (event.keyboardLocked != wasLocked) {
        wasLocked = event.keyboardLocked;
        if (!event.keyboardLocked) {
          startStableWait();
        } else {
          endStableWait();
        }
      }
    }

    private void await() throws InterruptedException, TimeoutException {
      try {
        if (!latch.await(30, TimeUnit.SECONDS)) {
          throw new TimeoutException();
        }
      } finally {
        client.removeKeyboardStatusListener(this);
        endStableWait();
        stableTimer.shutdownNow();
      }
    }

  }

  @Test
  public void shouldGetWelcomeScreenWhenConnect() throws Exception {
    awaitKeyboardUnlocked();
    assertThat(client.getScreenText())
        .isEqualTo(getFileContent("login-welcome-screen.txt"));
  }

  private String getFileContent(String resourceFile) throws IOException {
    return Resources.toString(Resources.getResource(resourceFile),
        Charsets.UTF_8);
  }

  @Test
  public void shouldGetUserMenuScreenWhenSendUserField() throws Exception {
    awaitKeyboardUnlocked();
    client.setFieldText(2, 1, "testusr");
    client.sendAID(AIDCommand.AID_ENTER, "ENTER");
    awaitKeyboardUnlocked();
    assertThat(client.getScreenText())
        .isEqualTo(getFileContent("user-menu-screen.txt"));
  }

}
