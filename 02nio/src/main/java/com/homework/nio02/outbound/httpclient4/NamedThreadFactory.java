package com.homework.nio02.outbound.httpclient4;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

public class NamedThreadFactory implements ThreadFactory {

  private final ThreadGroup group;
  private final AtomicInteger threadNumber = new AtomicInteger(1);
  private final String namePrefix;
  private final boolean daemon;

  public NamedThreadFactory(String namePrefix, boolean daemon) {
    this.namePrefix = namePrefix;
    this.daemon = daemon;
    SecurityManager sm = System.getSecurityManager();
    group = Objects.nonNull(sm) ? sm.getThreadGroup()
        : Thread.currentThread().getThreadGroup();
  }

  public NamedThreadFactory(String namePrefix) {
    this(namePrefix, false);
  }

  @Override
  public Thread newThread(@NotNull Runnable r) {
    String name = namePrefix + "-thread-" + threadNumber.getAndIncrement();
    Thread t = new Thread(group, r, name, 0);
    t.setDaemon(daemon);
    return t;
  }
}
