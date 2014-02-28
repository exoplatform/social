/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.common.service.utils;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.concurrent.TimeUnit;



public class LogWatch implements Cloneable {

  private static final long NANOS_IN_A_MILLI = 1000000L;

  private long              startTime;

  private long              nanoStartTime;

  private long              elapsedTime;

  private String            process;

  private String            message;

  public LogWatch() {
    this("", null);
  }

  public LogWatch(String tag) {
    this(tag, null);
  }

  public LogWatch(String tag, String message) {
    this(System.currentTimeMillis(), -1L, tag, message);
  }

  public LogWatch(long startTime, long elapsedTime, String process, String message) {
    this.startTime = startTime;
    this.nanoStartTime = (elapsedTime == -1L) ? System.nanoTime() : -1L;
    this.elapsedTime = elapsedTime;
    this.process = process;
    this.message = message;
  }

  public void start() {
    startTime = System.currentTimeMillis();
    nanoStartTime = System.nanoTime();
    elapsedTime = -1L;
  }
  
  public long getStartTime() {
    return startTime;
  }

  public long elapsedTime() {
    elapsedTime = (elapsedTime == -1L) ? (System.nanoTime() - nanoStartTime) / NANOS_IN_A_MILLI : elapsedTime;
    return elapsedTime;
  }
  
  public long getElapsedTime() {
    return elapsedTime;
  }
  
  public long elapsedTime(TimeUnit timeUnit) {
    return timeUnit.convert(getElapsedTime(), NANOSECONDS);
  }

  public String stop() {
    elapsedTime = elapsedTime();
    return this.toString();
  }
  
  public String getMessage() {
    return message;
  }

  public String toString() {
    String message = getMessage();
    return "start[" + getStartTime() + "] time[" + elapsedTime() + "] process[" + getProcess()
        + ((message == null) ? "]" : "] message[" + message + "]");
  }

  private String getProcess() {
    return process;
  }

  public LogWatch clone() {
    try {
      return (LogWatch) super.clone();
    } catch (CloneNotSupportedException cnse) {
      throw new Error("Unexpected CloneNotSupportedException");
    }
  }
  
  public String toString(long value, TimeUnit timeUnit) {
    return String.format("%s %s", value, abbreviate(timeUnit));
  }

  private static String abbreviate(TimeUnit unit) {
    switch (unit) {
      case NANOSECONDS:
        return "ns";
      case MILLISECONDS:
        return "ms";
      case SECONDS:
        return "sec";
      default:
        throw new AssertionError();
    }
  }


  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LogWatch)) {
      return false;
    }

    LogWatch stopWatch = (LogWatch) o;

    if (elapsedTime != stopWatch.elapsedTime) {
      return false;
    }
    if (startTime != stopWatch.startTime) {
      return false;
    }
    if (nanoStartTime != stopWatch.nanoStartTime) {
      return false;
    }
    if (message != null ? !message.equals(stopWatch.message) : stopWatch.message != null) {
      return false;
    }
    if (process != null ? !process.equals(stopWatch.process) : stopWatch.process != null) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int result = (int) (startTime ^ (startTime >>> 32));
    result = 31 * result + (int) (nanoStartTime ^ (nanoStartTime >>> 32));
    result = 31 * result + (int) (elapsedTime ^ (elapsedTime >>> 32));
    result = 31 * result + (process != null ? process.hashCode() : 0);
    result = 31 * result + (message != null ? message.hashCode() : 0);
    return result;
  }

}
