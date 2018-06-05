package com.bytezone.dm3270;

import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.display.ScreenPosition;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.utilities.Site;

import java.util.concurrent.*;

/**
 * Simple manual test of client interactions with a server.
 * <p/>
 * To run it use something like ManualTestClient myserver:23 2,3,test 2,5,pass
 */
public class ManualTestClient {

    public static void main(String[] args) {
        Screen screen = new Screen(new ScreenDimensions(24, 80), null, new TelnetState());
        screen.lockKeyboard("connect");
        String[] server = args[0].split(":");
        ConsolePane consolePane = new ConsolePane(screen, new Site("", server[0], Integer.valueOf(server[1]), false));
        consolePane.connect();
        try {
            new KeyboardUnlockListener(screen).await();
            if (args.length > 1) {
                for (int i = 1; i < args.length; i++) {
                    String[] field = args[i].split(",");
                    setFieldText(screen, Integer.valueOf(field[0]), Integer.valueOf(field[1]), field[2]);
                }
                consolePane.sendAID(AIDCommand.AID_ENTER, "ENTER");
                new KeyboardUnlockListener(screen).await();
            }
            System.out.println("Screen:\n" + getScreenText(screen));
        } catch (TimeoutException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            consolePane.disconnect();
        }
    }

    private static void setFieldText(Screen screen, int row, int column, String text) {
        int fieldLinearPosition = (row - 1) * screen.getScreenDimensions().columns + column - 1;
        Field field = screen.getFieldManager()
                .getFieldAt(fieldLinearPosition)
                .orElseThrow(() -> new IllegalArgumentException("Invalid field position " + row + "," + column));
        screen.setFieldText(field, text);
        screen.getScreenCursor().moveTo(fieldLinearPosition + text.length());
    }

    private static class KeyboardUnlockListener implements KeyboardStatusListener {

        private final Screen screen;
        private final CountDownLatch latch = new CountDownLatch(1);
        private final ScheduledExecutorService stableTimer = Executors.newSingleThreadScheduledExecutor();
        private ScheduledFuture stableTimeoutTask;
        private boolean wasLocked;

        private KeyboardUnlockListener(Screen screen) {
            this.screen = screen;
            wasLocked = screen.isKeyboardLocked();
            if (!screen.isKeyboardLocked()) {
                startStableWait();
            }
            screen.addKeyboardStatusChangeListener(this);
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
                screen.removeKeyboardStatusChangeListener(this);
                endStableWait();
                stableTimer.shutdownNow();
            }
        }

    }

    private static String getScreenText(Screen screen) {
        StringBuilder text = new StringBuilder();
        int pos = 0;
        for (ScreenPosition sp : screen.getPen()) {
            text.append(sp.getCharString());
            if (++pos % screen.getScreenDimensions().columns == 0) {
                text.append("\n");
            }
        }
        return text.toString();
    }

}
