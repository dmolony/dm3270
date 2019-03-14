package com.bytezone.dm3270;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class ConditionWaiter {

  private static final int STABLE_PERIOD_MILLIS = 1000;

  private final CountDownLatch lock = new CountDownLatch(1);
  protected final TerminalClient client;
  private final ScheduledExecutorService stableTimeoutExecutor;
  private ScheduledFuture stableTimeoutTask;
  private boolean ended;

  public ConditionWaiter(TerminalClient client, ScheduledExecutorService stableTimeoutExecutor) {
    this.client = client;
    this.stableTimeoutExecutor = stableTimeoutExecutor;
  }

  protected synchronized void startStablePeriod() {
    if (ended) {
      return;
    }
    endStablePeriod();
    stableTimeoutTask = stableTimeoutExecutor
        .schedule(lock::countDown, STABLE_PERIOD_MILLIS, TimeUnit.MILLISECONDS);
  }

  protected synchronized void endStablePeriod() {
    if (stableTimeoutTask != null) {
      stableTimeoutTask.cancel(false);
    }
  }

  public void await(long timeoutMillis) throws InterruptedException, TimeoutException {
    try {
      if (!lock.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
        throw new TimeoutException();
      }
    } finally {
      stop();
    }
  }

  private synchronized void cancelWait() {
    ended = true;
    lock.countDown();
    endStablePeriod();
  }

  protected void stop() {
    cancelWait();
  }

}