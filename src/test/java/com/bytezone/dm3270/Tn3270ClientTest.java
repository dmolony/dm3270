package com.bytezone.dm3270;

import static org.assertj.core.api.Assertions.assertThat;

import com.bytezone.dm3270.commands.AIDCommand;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
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

public class Tn3270ClientTest {

  private static final Logger LOG = LoggerFactory.getLogger(Tn3270ClientTest.class);
  private static final long TIMEOUT_MILLIS = 10000;
  private static final String SERVICE_HOST = "localhost";

  private VirtualTcpService service = new VirtualTcpService();
  private Tn3270Client client;
  private ExceptionWaiter exceptionWaiter;

  @Before
  public void setup() throws IOException {
    service.setSslEnabled(false);
    service.setFlow(Flow.fromYml(new File(getResourceFilePath("/login.yml"))));
    service.start();
    client = new Tn3270Client();
    client.setConnectionTimeoutMillis(5000);
    exceptionWaiter = new ExceptionWaiter();
    client.setExceptionHandler(exceptionWaiter);
    client.connect(SERVICE_HOST, service.getPort(), TerminalType.DEFAULT_TERMINAL_TYPE);
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
  public void shouldUnlockKeyboardWhenConnect() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    client.addKeyboardStatusListener(e -> {
      if (!e.keyboardLocked) {
        latch.countDown();
      }
    });
    if (!client.isKeyboardLocked()) {
      latch.countDown();
    }
    if (!latch.await(TIMEOUT_MILLIS, TimeUnit.SECONDS)) {
      throw new TimeoutException();
    }
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnect() throws Exception {
    awaitLoginScreen();
    assertThat(client.getScreenText())
        .isEqualTo(getFileContent("login-welcome-screen.txt"));
  }

  private void awaitLoginScreen() throws TimeoutException, InterruptedException {
    awaitScreenContains("ENTER USERID");
  }

  private void awaitScreenContains(String text) throws InterruptedException, TimeoutException {
    CountDownLatch latch = new CountDownLatch(1);
    client.addScreenChangeListener(e -> {
      LOG.debug("Received screen {}", text);
      if (client.getScreenText().contains(text)) {
        latch.countDown();
      }
    });
    if (client.getScreenText().contains(text)) {
      latch.countDown();
    }
    if (!latch.await(TIMEOUT_MILLIS, TimeUnit.SECONDS)) {
      throw new TimeoutException();
    }
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

    client = new Tn3270Client();
    client.setSocketFactory(buildSslContext().getSocketFactory());
    client.connect(SERVICE_HOST, service.getPort(), TerminalType.DEFAULT_TERMINAL_TYPE);

    awaitLoginScreen();
    assertThat(client.getScreenText())
        .isEqualTo(getFileContent("login-welcome-screen.txt"));
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
    awaitScreenContains("TSO/E LOGON");
    assertThat(client.getScreenText())
        .isEqualTo(getFileContent("user-menu-screen.txt"));
  }

  private void sendUserField() {
    client.setFieldText(2, 1, "testusr");
    client.sendAID(AIDCommand.AID_ENTER, "ENTER");
  }

  @Test
  public void shouldSendExceptionToExceptionHandlerWhenConnectWithInvalidPort() throws Exception {
    client.connect(SERVICE_HOST, 1, TerminalType.DEFAULT_TERMINAL_TYPE);
    exceptionWaiter.awaitException();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenSendIncorrectFieldPosition()
      throws Exception {
    awaitLoginScreen();
    client.setFieldText(0,1, "test");
  }

  @Test
  public void shouldSendCloseToExceptionHandlerWhenServerDown() throws Exception {
    awaitLoginScreen();
    service.stop(TIMEOUT_MILLIS);
    sendUserField();
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
