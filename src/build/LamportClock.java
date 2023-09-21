package build;

/**
 * LamportClock.java
 * Lamport Clocks are used to maintain synchronisation between process
 * using a single atomic integer timestamp.
 * - A send message will increment the clock by 1.
 * - A receive message will increment the clock by max(current clock, event time)
 *
 * The clock will be saved to a file called 'clock'
 * and will be restored upon restarting.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public class LamportClock {

  public LamportClock() {
    restoreClock();
  }

  private AtomicInteger timeStamp = new AtomicInteger(0); // To be incremented

  /**
   * Saves current timestamp to the file 'clock'.
   */
  public synchronized void saveClock() {
    try {
      Path file = Paths.get("clock");
      BufferedWriter writer = Files.newBufferedWriter(file);
      writer.write(Integer.toString(getTime()));
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * If the file 'clock' exists
   * - Restores the timestamp
   */
  public void restoreClock() {
    File f = new File("clock");
    if (f.exists() && f.length() != 0) {
      try {
        Path file = Paths.get("clock");
        BufferedReader reader = Files.newBufferedReader(file);
        this.setTime(Integer.parseInt(reader.readLine()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Gets current timestamp
   */
  public Integer getTime() {
    return timeStamp.get();
  }

  /**
   * Sets new time stamp to provided integer.
   */
  public void setTime(Integer time) {
    timeStamp.set(time);
  }

  /**
   * Increments timestamp by 1
   */
  public synchronized void sendEvent() {
    timeStamp.incrementAndGet();
    System.out.println("sendEvent - add 1: " + timeStamp);
    saveClock();
  }

  /**
   * Increments timestamp by max of:
   * - current timestamp
   * - and event time
   */
  public synchronized void receiveEvent(int eventTime) {
    timeStamp.set(Math.max(getTime(), eventTime) + 1);
    System.out.println("receiveEvent - add max: " + timeStamp);
    saveClock();
  }
}
