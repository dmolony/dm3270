package com.bytezone.dm3270;

import static org.assertj.core.api.Assertions.assertThat;

import com.bytezone.dm3270.commands.AIDCommand;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.wiresham.Flow;
import us.abstracta.wiresham.VirtualTcpService;

public class TerminalClientTest {

  private static final Logger LOG = LoggerFactory.getLogger(TerminalClientTest.class);
  private static final long TIMEOUT_MILLIS = 10000;
  private static final String SERVICE_HOST = "localhost";

  private VirtualTcpService service = new VirtualTcpService();
  private TerminalClient client;
  private ExceptionWaiter exceptionWaiter;

  @Before
  public void setup() throws IOException {
    service.setSslEnabled(false);
    service.setFlow(Flow.fromYml(new File(getResourceFilePath("/login.yml"))));
    service.start();
    client = new TerminalClient();
    client.setConnectionTimeoutMillis(5000);
    exceptionWaiter = new ExceptionWaiter();
    client.setExceptionHandler(exceptionWaiter);
    client.connect(SERVICE_HOST, service.getPort());
  }

  private static class ExceptionWaiter implements ExceptionHandler {

    private CountDownLatch exceptionLatch = new CountDownLatch(1);
    private CountDownLatch closeLatch = new CountDownLatch(1);

    @Override
    public void onException(Exception ex) {
      exceptionLatch.countDown();
    }

    @Override
    public void onConnectionClosed() {
      closeLatch.countDown();
    }

    private void awaitException() throws InterruptedException {
      assertThat(exceptionLatch.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue();
    }

    private void awaitClose() throws InterruptedException {
      assertThat(closeLatch.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue();
    }

  }


  private String getResourceFilePath(String resourcePath) {
    return getClass().getResource(resourcePath).getFile();
  }

  @After
  public void teardown() throws Exception {
    client.disconnect();
    service.stop(TIMEOUT_MILLIS);
  }

  @Test
  public void shouldGetUnlockedKeyboardWhenConnect() throws Exception {
    awaitKeyboardUnlock();
    assertThat(client.isKeyboardLocked()).isFalse();
  }

  private void awaitKeyboardUnlock() throws InterruptedException, TimeoutException {
    CountDownLatch latch = new CountDownLatch(1);
    client.addKeyboardStatusListener(e -> {
      if (!e.keyboardLocked) {
        latch.countDown();
      }
    });
    if (!client.isKeyboardLocked()) {
      latch.countDown();
    }
    if (!latch.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
      throw new TimeoutException();
    }
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnect() throws Exception {
    awaitLoginScreen();
    assertThat(client.getScreenText())
        .isEqualTo(getWelcomeScreen());
  }

  private void awaitLoginScreen() throws TimeoutException, InterruptedException {
    awaitScreenContains("ENTER USERID");
  }

  private void awaitScreenContains(String text) throws InterruptedException, TimeoutException {
    CountDownLatch latch = new CountDownLatch(1);
    client.addScreenChangeListener(e -> {
      String screen = client.getScreenText();
      LOG.debug("Received screen {}", screen);
      if (screen.contains(text)) {
        latch.countDown();
      }
    });
    if (client.getScreenText().contains(text)) {
      latch.countDown();
    }
    if (!latch.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
      throw new TimeoutException();
    }
  }

  private String getWelcomeScreen() throws IOException {
    return getFileContent("login-welcome-screen.txt");
  }

  private String getFileContent(String resourceFile) throws IOException {
    return Resources.toString(Resources.getResource(resourceFile),
        Charsets.UTF_8);
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnectWithSsl() throws Exception {
    awaitLoginScreen();
    client.disconnect();
    service.stop(TIMEOUT_MILLIS);

    service.setSslEnabled(true);
    System.setProperty("javax.net.ssl.keyStore", getResourceFilePath("/keystore.jks"));
    System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
    service.start();

    client = new TerminalClient();
    client.setSocketFactory(buildSslContext().getSocketFactory());
    client.connect(SERVICE_HOST, service.getPort());

    awaitLoginScreen();
    assertThat(client.getScreenText())
        .isEqualTo(getWelcomeScreen());
  }

  private SSLContext buildSslContext() throws GeneralSecurityException {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    TrustManager trustManager = new X509TrustManager() {

      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
      }

      public void checkClientTrusted(
          java.security.cert.X509Certificate[] certs, String authType) {
      }

      public void checkServerTrusted(
          java.security.cert.X509Certificate[] certs, String authType) {
      }
    };
    sslContext.init(null, new TrustManager[]{trustManager},
        new SecureRandom());
    return sslContext;
  }

  @Test
  public void shouldGetUserMenuScreenWhenSendUserField() throws Exception {
    awaitLoginScreen();
    sendUserField();
    awaitMenuScreen();
    assertThat(client.getScreenText())
        .isEqualTo(getFileContent("user-menu-screen.txt"));
  }

  private void awaitMenuScreen() throws InterruptedException, TimeoutException {
    awaitScreenContains("TSO/E LOGON");
  }

  private void sendUserField() {
    client.setFieldText(2, 1, "testusr");
    client.sendAID(AIDCommand.AID_ENTER, "ENTER");
  }

  @Test
  public void shouldGetNotSoundedAlarmWhenWhenConnect() throws Exception {
    awaitLoginScreen();
    assertThat(client.resetAlarm()).isFalse();
  }

  @Test
  public void shouldGetSoundedAlarmWhenWhenSendUserField() throws Exception {
    awaitLoginScreen();
    sendUserField();
    awaitMenuScreen();
    assertThat(client.resetAlarm()).isTrue();
  }

  @Test
  public void shouldGetNotSoundedAlarmWhenWhenSendUserFieldAndResetAlarm() throws Exception {
    awaitLoginScreen();
    sendUserField();
    awaitMenuScreen();
    client.resetAlarm();
    assertThat(client.resetAlarm()).isFalse();
  }

  @Test
  public void shouldGetFieldPositionWhenGetCursorPositionAfterConnect() throws Exception {
    Point fieldPosition = new Point(1, 2);
    awaitCursorPosition(fieldPosition);
    assertThat(client.getCursorPosition()).isEqualTo(Optional.of(fieldPosition));
  }

  private void awaitCursorPosition(Point position) throws InterruptedException, TimeoutException {
    CountDownLatch latch = new CountDownLatch(1);
    client.addCursorMoveListener((newPos, oldPos, field) -> {
      if (position.equals(client.getCursorPosition().orElse(null))) {
        latch.countDown();
      }
    });
    if (!client.isKeyboardLocked()) {
      latch.countDown();
    }
    if (!latch.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
      throw new TimeoutException();
    }
  }

  @Test
  public void shouldSendExceptionToExceptionHandlerWhenConnectWithInvalidPort() throws Exception {
    client.connect(SERVICE_HOST, 1);
    exceptionWaiter.awaitException();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenSendIncorrectFieldPosition()
      throws Exception {
    awaitLoginScreen();
    client.setFieldText(0, 1, "test");
  }

  @Test
  public void shouldSendCloseToExceptionHandlerWhenServerDown() throws Exception {
    awaitLoginScreen();
    service.stop(TIMEOUT_MILLIS);
    exceptionWaiter.awaitClose();
  }

  @Test
  public void shouldSendExceptionToExceptionHandlerWhenSendAndServerDown() throws Exception {
    awaitLoginScreen();
    service.stop(TIMEOUT_MILLIS);
    sendUserField();
    exceptionWaiter.awaitException();
  }

}
