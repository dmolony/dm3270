package com.bytezone.dm3270;

import static org.assertj.core.api.Assertions.assertThat;

import com.bytezone.dm3270.commands.AIDCommand;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
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
  private ScheduledExecutorService stableTimeoutExecutor = Executors
      .newSingleThreadScheduledExecutor();

  @Rule
  public TestRule watchman = new TestWatcher() {
    @Override
    public void starting(Description description) {
      LOG.debug("Starting {}", description.getMethodName());
    }

    @Override
    public void finished(Description description) {
      LOG.debug("Finished {}", description.getMethodName());
    }
  };

  @Before
  public void setup() throws IOException {
    service.setSslEnabled(false);
    setServiceFlowFromFile("/login.yml");
    service.start();
    client = new TerminalClient();
    client.setConnectionTimeoutMillis(5000);
    exceptionWaiter = new ExceptionWaiter();
    client.setExceptionHandler(exceptionWaiter);
    connectClient();
  }

  private void connectClient() {
    client.connect(SERVICE_HOST, service.getPort());
    client.addScreenChangeListener(
        screenWatcher -> LOG.debug("Screen updated, cursor={}, alarm={}, screen:{}",
            client.getCursorPosition().orElse(null), client.isAlarmOn(), client.getScreenText()));
  }

  private void setServiceFlowFromFile(String s) throws FileNotFoundException {
    service.setFlow(Flow.fromYml(new File(getResourceFilePath(s))));
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
    new UnlockWaiter(TIMEOUT_MILLIS, client, stableTimeoutExecutor).await();
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnect() throws Exception {
    awaitKeyboardUnlock();
    assertThat(client.getScreenText())
        .isEqualTo(getWelcomeScreen());
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
    setupSslConnection();
    awaitKeyboardUnlock();
    assertThat(client.getScreenText())
        .isEqualTo(getWelcomeScreen());
  }

  private void setupSslConnection() throws Exception {
    awaitKeyboardUnlock();
    teardown();

    service.setSslEnabled(true);
    System.setProperty("javax.net.ssl.keyStore", getResourceFilePath("/keystore.jks"));
    System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
    service.start();

    client = new TerminalClient();
    client.setSocketFactory(buildSslContext().getSocketFactory());
    connectClient();
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
  public void shouldGetUserMenuScreenWhenSendUserFieldByCoord() throws Exception {
    awaitKeyboardUnlock();
    sendUserFieldByCoord();
    awaitKeyboardUnlock();
    assertThat(client.getScreenText())
        .isEqualTo(getFileContent("user-menu-screen.txt"));
  }

  private void sendUserFieldByCoord() {
    sendFieldByCoord(1, 27, "testusr");
  }

  private void sendFieldByCoord(int row, int column, String text) {
    client.setFieldTextByCoord(row, column, text);
    client.sendAID(AIDCommand.AID_ENTER, "ENTER");
  }

  @Test
  public void shouldGetLoginSuccessScreenWhenSendPasswordFieldByProtectedLabel() throws Exception {
    awaitKeyboardUnlock();
    sendUserFieldByCoord();
    awaitKeyboardUnlock();
    sendFieldByLabel("Password", "testpsw");
    awaitSuccessScreen();
  }

  private void sendFieldByLabel(String label, String text) {
    client.setFieldTextByLabel(label, text);
    client.sendAID(AIDCommand.AID_ENTER, "ENTER");
  }

  private void awaitSuccessScreen() throws InterruptedException, TimeoutException {
    awaitScreenContains("READY");
  }

  private void awaitScreenContains(String text) throws TimeoutException, InterruptedException {
    new ScreenTextWaiter(text, TIMEOUT_MILLIS, client, stableTimeoutExecutor).await();
  }

  @Test
  public void shouldGetUserMenuScreenWhenSendUserFieldByUnprotectedLabel() throws Exception {
    awaitKeyboardUnlock();
    sendFieldByLabel("ENTER USERID", "testusr");
    awaitKeyboardUnlock();
    assertThat(client.getScreenText())
        .isEqualTo(getFileContent("user-menu-screen.txt"));
  }

  @Test
  public void shouldGetWelcomeMessageWhenSendUserInScreenWithoutFields() throws Exception {
    setupLoginWithoutFields();
    awaitKeyboardUnlock();
    sendFieldByCoord(20, 48, "testusr");
    awaitKeyboardUnlock();
    sendFieldByCoord(1, 1, "testusr");
    awaitKeyboardUnlock();
  }

  private void setupLoginWithoutFields() throws Exception {
    awaitKeyboardUnlock();
    teardown();

    setServiceFlowFromFile("/login-without-fields.yml");
    service.start();

    client = new TerminalClient();
    client.setUsesExtended3270(true);
    connectClient();
  }

  @Test
  public void shouldGetNotSoundedAlarmWhenWhenConnect() throws Exception {
    awaitKeyboardUnlock();
    assertThat(client.resetAlarm()).isFalse();
  }

  @Test
  public void shouldGetSoundedAlarmWhenWhenSendUserField() throws Exception {
    awaitKeyboardUnlock();
    sendUserFieldByCoord();
    awaitKeyboardUnlock();
    assertThat(client.resetAlarm()).isTrue();
  }

  @Test
  public void shouldGetNotSoundedAlarmWhenWhenSendUserFieldAndResetAlarm() throws Exception {
    awaitKeyboardUnlock();
    sendUserFieldByCoord();
    awaitKeyboardUnlock();
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
    awaitKeyboardUnlock();
    client.setFieldTextByCoord(0, 1, "test");
  }

  @Test
  public void shouldSendCloseToExceptionHandlerWhenServerDown() throws Exception {
    awaitKeyboardUnlock();
    service.stop(TIMEOUT_MILLIS);
    exceptionWaiter.awaitClose();
  }

  @Test
  public void shouldSendExceptionToExceptionHandlerWhenSendAndServerDown() throws Exception {
    awaitKeyboardUnlock();
    service.stop(TIMEOUT_MILLIS);
    sendUserFieldByCoord();
    exceptionWaiter.awaitException();
  }

  @Test
  public void shouldGetLoginSuccessScreenWhenLoginWithSscpLuData() throws Exception {
    setupLoginWithSscpLuData();
    awaitKeyboardUnlock();
    sendFieldByCoord(11, 25, "testapp");
    awaitKeyboardUnlock();
    client.setFieldTextByCoord(12, 21, "testusr");
    client.setFieldTextByCoord(13, 21, "testpsw");
    client.sendAID(AIDCommand.AID_ENTER, "ENTER");
    awaitKeyboardUnlock();
    assertThat(client.getScreenText())
        .isEqualTo(getFileContent("sscplu-login-success-screen.txt"));
  }

  private void setupLoginWithSscpLuData() throws Exception {
    awaitKeyboardUnlock();
    teardown();

    setServiceFlowFromFile("/sscplu-login.yml");
    service.start();

    client = new TerminalClient();
    client.setUsesExtended3270(true);
    connectClient();
  }

}
