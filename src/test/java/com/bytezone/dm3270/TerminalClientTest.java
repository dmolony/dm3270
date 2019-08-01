package com.bytezone.dm3270;

import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.display.ScreenPosition;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.wiresham.Flow;
import us.abstracta.wiresham.VirtualTcpService;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TerminalClientTest {

  private static final Logger LOG = LoggerFactory.getLogger(TerminalClientTest.class);
  private static final int TERMINAL_MODEL_TYPE_TWO = 2;
  private static final int TERMINAL_MODEL_TYPE_THREE = 3;
  private static final int TERMINAL_MODEL_TYPE_M_FIVE = 5;
  private static final ScreenDimensions SCREEN_DIMENSIONS_M_FIVE = new ScreenDimensions(27, 132);
  private static final ScreenDimensions SCREEN_DIMENSIONS = new ScreenDimensions(24, 80);
  private static final long TIMEOUT_MILLIS = 10000;
  private static final String SERVICE_HOST = "localhost";
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
  private VirtualTcpService service = new VirtualTcpService();
  private TerminalClient client;
  private ExceptionWaiter exceptionWaiter;
  private ScheduledExecutorService stableTimeoutExecutor = Executors
      .newSingleThreadScheduledExecutor();
  @Mock
  private Screen screenMock;

  @Before
  public void setup() throws IOException {
    service.setSslEnabled(false);
    setServiceFlowFromFile("/login.yml");
    service.start();
    client = new TerminalClient(TERMINAL_MODEL_TYPE_TWO, SCREEN_DIMENSIONS);
    client.setConnectionTimeoutMillis(5000);
    exceptionWaiter = new ExceptionWaiter();
    client.setConnectionListener(exceptionWaiter);
    connectClient();
  }

  private static class ExceptionWaiter implements ConnectionListener {

    private CountDownLatch exceptionLatch = new CountDownLatch(1);

    private CountDownLatch closeLatch = new CountDownLatch(1);

    @Override
    public void onConnection() {
    }

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

  private void connectClient() {
    client.connect(SERVICE_HOST, service.getPort());
    client.addScreenChangeListener(
        screenWatcher -> LOG.debug("Screen updated, cursor={}, alarm={}, screen:{}",
            client.getCursorPosition().orElse(null), client.isAlarmOn(), getScreenText()));
  }

  private String getScreenText() {
    return client.getScreenText().replace('\u0000', ' ');
  }

  private void setServiceFlowFromFile(String s) throws FileNotFoundException {
    service.setFlow(Flow.fromYml(new File(getResourceFilePath(s))));
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
    new UnlockWaiter(client, stableTimeoutExecutor).await(TIMEOUT_MILLIS);
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnect() throws Exception {
    awaitKeyboardUnlock();
    assertThat(getScreenText())
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
  public void shouldGetWelcomeScreenWithWrongCharset() throws Exception {
    setupExtendedFlow(TERMINAL_MODEL_TYPE_THREE, SCREEN_DIMENSIONS, "/login-special-characters.yml");

    awaitKeyboardUnlock();
    assertThat(getScreenText())
            .isEqualTo( getFileContent("login-special-character-charset-CP1047.txt"));
  }

  @Test
  public void shouldGetWelcomeScreenWithRightCharset() throws Exception {
    setupExtendedFlowWithCharsetCP1147(TERMINAL_MODEL_TYPE_THREE, SCREEN_DIMENSIONS, "/login-special-characters.yml");
    awaitKeyboardUnlock();
    assertThat(getScreenText())
            .isEqualTo( getFileContent("login-special-character-charset-CP1147.txt"));
  }

  private void setupExtendedFlowWithCharsetCP1147(int terminalType, ScreenDimensions screenDimensions,
                                 String filePath)
          throws Exception {
    awaitKeyboardUnlock();
    teardown();
    setServiceFlowFromFile(filePath);
    service.start();
    client = new TerminalClient(terminalType, screenDimensions,Charset.CP1147);
    client.setUsesExtended3270(true);
    connectClient();
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnectWithSsl() throws Exception {
    setupSslConnection();
    awaitKeyboardUnlock();
    assertThat(getScreenText())
        .isEqualTo(getWelcomeScreen());
  }

  private void setupSslConnection() throws Exception {
    awaitKeyboardUnlock();
    teardown();

    service.setSslEnabled(true);
    System.setProperty("javax.net.ssl.keyStore", getResourceFilePath("/keystore.jks"));
    System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
    service.start();

    client = new TerminalClient(TERMINAL_MODEL_TYPE_TWO, SCREEN_DIMENSIONS);
    client.setSocketFactory(buildSslContext().getSocketFactory());
    connectClient();
  }

  private SSLContext buildSslContext() throws GeneralSecurityException {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    TrustManager trustManager = new X509TrustManager() {

      public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
      }

      public void checkClientTrusted(
          X509Certificate[] certs, String authType) {
      }

      public void checkServerTrusted(
          X509Certificate[] certs, String authType) {
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
    assertThat(getScreenText())
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
    new ScreenTextWaiter(text, client, stableTimeoutExecutor).await(TIMEOUT_MILLIS);
  }

  @Test
  public void shouldGetUserMenuScreenWhenSendUserFieldByUnprotectedLabel() throws Exception {
    awaitKeyboardUnlock();
    sendFieldByLabel("ENTER USERID", "testusr");
    awaitKeyboardUnlock();
    assertThat(getScreenText())
        .isEqualTo(getFileContent("user-menu-screen.txt"));
  }

  @Test
  public void shouldGetWelcomeMessageWhenSendUserInScreenWithoutFields() throws Exception {
    setupExtendedFlow(TERMINAL_MODEL_TYPE_TWO, SCREEN_DIMENSIONS, "/login-without-fields.yml");
    awaitKeyboardUnlock();
    sendFieldByCoord(20, 48, "testusr");
    awaitKeyboardUnlock();
    sendFieldByCoord(1, 1, "testusr");
    awaitKeyboardUnlock();
  }

  private void setupExtendedFlow(int terminalType, ScreenDimensions screenDimensions,
      String filePath)
      throws Exception {
    awaitKeyboardUnlock();
    teardown();
    setServiceFlowFromFile(filePath);
    service.start();
    client = new TerminalClient(terminalType, screenDimensions);
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
    setupExtendedFlow(TERMINAL_MODEL_TYPE_TWO, SCREEN_DIMENSIONS, "/sscplu-login.yml");
    awaitKeyboardUnlock();
    sendFieldByCoord(11, 25, "testapp");
    awaitKeyboardUnlock();
    client.setFieldTextByCoord(12, 21, "testusr");
    client.setFieldTextByCoord(13, 21, "testpsw");
    client.sendAID(AIDCommand.AID_ENTER, "ENTER");
    awaitKeyboardUnlock();
    assertThat(getScreenText())
        .isEqualTo(getFileContent("sscplu-login-success-screen.txt"));
  }

  @Test
  public void shouldGetCorrectFieldsWhenGetFields() throws Exception {
    when(screenMock.validate(anyInt())).thenAnswer(
        (Answer<Integer>) invocationOnMock -> (Integer) invocationOnMock.getArguments()[0]);
    awaitKeyboardUnlock();
    sendUserFieldByCoord();
    awaitKeyboardUnlock();
    assertEquals(expectedFields(), client.getFields());
  }

  private List<Field> expectedFields() {
    ScreenBuilder screenBuilder = new ScreenBuilder()
        .withField(new FieldBuilder("------------------------------- TSO/E LOGON ----------------" +
            "-------------------").withHighIntensity().withSelectorPenDetectable())
        .withField(
            new FieldBuilder("                                                             " +
                "                  ").withHighIntensity().withSelectorPenDetectable())
        .withField(
            new FieldBuilder("                                                             " +
                "                   \u0000\u0000").withHighIntensity().withSelectorPenDetectable())
        .withField(new FieldBuilder("Enter LOGON parameters below:" + buildNullString(18))
            .withHighIntensity().withSelectorPenDetectable())
        .withField(
            new FieldBuilder("RACF LOGON parameters:" + buildNullString(88)).withHighIntensity()
                .withSelectorPenDetectable())
        .withField(new FieldBuilder(" Userid    ===>"))
        .withField(new FieldBuilder("TESTUSR ").withHighIntensity())
        .withField(new FieldBuilder(buildNullString(22)).withNumeric())
        .withField(new FieldBuilder(" Seclabel     ===>").withNumeric().withHidden())
        .withField(new FieldBuilder("        ").withNumeric().withHidden())
        .withField(new FieldBuilder(buildNullString(83)).withNumeric())
        .withField(new FieldBuilder(" Password  ===>"))
        .withField(new FieldBuilder("        ").withNotProtected().withHidden())
        .withField(new FieldBuilder(buildNullString(22)).withNumeric())
        .withField(new FieldBuilder(" New Password ===>"))
        .withField(new FieldBuilder("        ").withNotProtected().withHidden())
        .withField(new FieldBuilder(buildNullString(83)).withNumeric())
        .withField(new FieldBuilder(" Procedure ===>"))
        .withField(new FieldBuilder("PROC394 ").withNotProtected().withHighIntensity()
            .withSelectorPenDetectable())
        .withField(new FieldBuilder(buildNullString(22)).withNumeric())
        .withField(new FieldBuilder(" Group Ident  ===>"))
        .withField(new FieldBuilder("        ").withNotProtected().withHighIntensity()
            .withSelectorPenDetectable())
        .withField(new FieldBuilder(buildNullString(83)).withNumeric())
        .withField(new FieldBuilder(" Acct Nmbr ===>"))
        .withField(new FieldBuilder("1000000                                 ").withNotProtected()
            .withHighIntensity().withSelectorPenDetectable())
        .withField(new FieldBuilder(buildNullString(102)).withNumeric())
        .withField(new FieldBuilder(" Size      ===>"))
        .withField(new FieldBuilder("4096   ").withNotProtected().withHighIntensity()
            .withSelectorPenDetectable())
        .withField(new FieldBuilder(buildNullString(135)).withNumeric())
        .withField(new FieldBuilder(" Perform   ===>"))
        .withField(new FieldBuilder("   ").withNotProtected().withHighIntensity()
            .withSelectorPenDetectable())
        .withField(new FieldBuilder(buildNullString(139)).withNumeric())
        .withField(new FieldBuilder(" Command   ===>"))
        .withField(new FieldBuilder("                                                            " +
            "                    ").withNotProtected().withHighIntensity()
            .withSelectorPenDetectable())
        .withField(new FieldBuilder(buildNullString(63)).withNumeric())
        .withField(
            new FieldBuilder("Enter an 'S' before each option desired below:").withHighIntensity()
                .withSelectorPenDetectable())
        .withField(new FieldBuilder(buildNullString(36)))
        .withField(new FieldBuilder(" ").withHighIntensity().withSelectorPenDetectable())
        .withField(new FieldBuilder(" ").withNotProtected().withHighIntensity()
            .withSelectorPenDetectable())
        .withField(new FieldBuilder("-Nomail").withNumeric())
        .withField(new FieldBuilder("\u0000\u0000\u0000"))
        .withField(new FieldBuilder(" ").withHighIntensity().withSelectorPenDetectable())
        .withField(new FieldBuilder(" ").withNotProtected().withHighIntensity()
            .withSelectorPenDetectable())
        .withField(new FieldBuilder("-Nonotice").withNumeric())
        .withField(new FieldBuilder("\u0000\u0000"))
        .withField(new FieldBuilder(" ").withHighIntensity().withSelectorPenDetectable())
        .withField(new FieldBuilder(" ").withNotProtected().withHighIntensity()
            .withSelectorPenDetectable())
        .withField(new FieldBuilder("-Reconnect").withNumeric())
        .withField(new FieldBuilder("\u0000\u0000"))
        .withField(new FieldBuilder(" ").withHighIntensity().withSelectorPenDetectable())
        .withField(new FieldBuilder(" ").withNotProtected().withHighIntensity()
            .withSelectorPenDetectable())
        .withField(new FieldBuilder("-OIDcard ").withNumeric())
        .withField(new FieldBuilder(buildNullString(87)))
        .withField(
            new FieldBuilder("PF1/PF13 ==> Help    PF3/PF15 ==> Logoff    PA1 ==> Attention" +
                "    PA2 ==> Reshow").withHighIntensity().withSelectorPenDetectable())
        .withField(
            new FieldBuilder("You may request specific help information by entering a '?' in" +
                " any entry field\u0000").withHighIntensity().withSelectorPenDetectable());
    return screenBuilder.build();
  }

  private String buildNullString(int count) {
    return new String(new char[count]);
  }

  private static final class ScreenBuilder {

    private final List<Field> fields = new ArrayList<>();

    private Field lastField = null;

    private ScreenBuilder withField(FieldBuilder builder) {
      Field currField = lastField != null ? builder.withStartPosition(lastField.getFirstLocation()
          + lastField.getText().length()).build() : builder.build();
      fields.add(currField);
      lastField = currField;
      return this;
    }

    private List<Field> build() {
      return fields;
    }


  }

  private final class FieldBuilder {

    private int startPosition;

    private String text;
    private boolean isProtected = true;
    private boolean isNumeric = false;
    private boolean isHidden = false;
    private boolean isHighIntensity = false;
    private boolean isModified = false;
    private boolean selectorPenDetectable = false;

    private FieldBuilder(String text) {
      this.text = text;
    }

    private FieldBuilder withStartPosition(int pos) {
      this.startPosition = pos;
      return this;
    }

    private FieldBuilder withNotProtected() {
      isProtected = false;
      return this;
    }

    private FieldBuilder withNumeric() {
      isNumeric = true;
      return this;
    }

    private FieldBuilder withHidden() {
      isHidden = true;
      return this;
    }

    private FieldBuilder withModified() {
      isModified = true;
      return this;
    }

    private FieldBuilder withHighIntensity() {
      isHighIntensity = true;
      return this;
    }


    private FieldBuilder withSelectorPenDetectable() {
      selectorPenDetectable = true;
      return this;
    }

    private Field build() {
      try {
        List<ScreenPosition> positions = new ArrayList<>();
        for (int i = 0; i <= text.length(); i++) {
          positions.add(new ScreenPosition(startPosition + i, null, Charset.CP1047));
        }
        positions.get(0).setStartField(buildStartFieldAttribute());
        Field f = new Field(TerminalClientTest.this.screenMock, positions);
        f.setText(text.getBytes(Charset.CP1047.name()));
        return f;
      } catch (UnsupportedEncodingException e) {
        // As this is not expected to happen, we just throw RuntimeException.
        throw new RuntimeException(e);
      }
    }

    private StartFieldAttribute buildStartFieldAttribute() {
      byte b = 0;
      if (isProtected) {
        b |= 0x20;
      }
      if (isNumeric) {
        b |= 0x10;
      }
      if (isModified) {
        b |= 0x01;
      }
      if (isHighIntensity) {
        b |= 0x08;
      } else if (isHidden) {
        b |= 0x0C;
      } else if (selectorPenDetectable) {
        b |= 0x04;
      }
      return new StartFieldAttribute(b);
    }

  }

  @Test
  public void shouldShowMenuScreenWithDifferentTerminalType() throws Exception {
    setupExtendedFlow(TERMINAL_MODEL_TYPE_M_FIVE, SCREEN_DIMENSIONS_M_FIVE,
        "/login-3270-model-5.yml");
    awaitKeyboardUnlock();
    sendUserFieldByCoord();
    awaitKeyboardUnlock();
    assertThat(getScreenText()).isEqualTo(getFileContent("user-menu-screen.txt"));
  }
}
