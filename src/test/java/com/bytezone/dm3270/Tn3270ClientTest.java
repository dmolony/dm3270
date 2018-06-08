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
import us.abstracta.wiresham.Flow;
import us.abstracta.wiresham.VirtualTcpService;

public class Tn3270ClientTest {

  private static final long TIMEOUT_MILLIS = 10000;

  private VirtualTcpService service = new VirtualTcpService();
  private Tn3270Client client;

  @Before
  public void setup() throws IOException {
    service.setSslEnabled(false);
    service.setFlow(Flow.fromYml(new File(getResourceFilePath("/login.yml"))));
    service.start();
    client = new Tn3270Client();
    client.connect("localhost", service.getPort(), TerminalType.DEFAULT_TERMINAL_TYPE);
  }

  private String getResourceFilePath(String resourcePath) {
    return getClass().getResource(resourcePath).getFile();
  }

  @After
  public void teardown() throws Exception {
    client.disconnect();
    service.stop(TIMEOUT_MILLIS);
  }

  @Test(timeout = TIMEOUT_MILLIS)
  public void shouldUnlockKeyboardAfterConnect() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    client.addKeyboardStatusListener(e -> {
      if (!e.keyboardLocked) {
        latch.countDown();
      }
    });
    if (!client.isKeyboardLocked()) {
      latch.countDown();
    }
    if (!latch.await(30, TimeUnit.SECONDS)) {
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
      if (client.getScreenText().contains(text)) {
        latch.countDown();
      }
    });
    if (client.getScreenText().contains(text)) {
      latch.countDown();
    }
    if (!latch.await(30, TimeUnit.SECONDS)) {
      throw new TimeoutException();
    }
  }

  private String getFileContent(String resourceFile) throws IOException {
    return Resources.toString(Resources.getResource(resourceFile),
        Charsets.UTF_8);
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnectWithSsl() throws Exception {
    client.disconnect();
    service.stop(TIMEOUT_MILLIS);

    service.setSslEnabled(true);
    System.setProperty("javax.net.ssl.keyStore", getResourceFilePath("/keystore.jks"));
    System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
    service.start();

    client = new Tn3270Client();
    client.setSocketFactory(buildSslContext().getSocketFactory());
    client.connect("localhost", service.getPort(), TerminalType.DEFAULT_TERMINAL_TYPE);

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
    client.setFieldText(2, 1, "testusr");
    client.sendAID(AIDCommand.AID_ENTER, "ENTER");
    awaitScreenContains("TSO/E LOGON");
    assertThat(client.getScreenText())
        .isEqualTo(getFileContent("user-menu-screen.txt"));
  }

}
