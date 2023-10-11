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

public class LamportClock {

  private static Integer timeStamp = 0; // To be incremented;

  public static LamportClock getInstance() {
    return new LamportClock();
  }

  private LamportClock() {
    restoreClock();
  }

  /**
   * Saves current timestamp to the file 'clock'.
   */
  private synchronized void saveClock(Integer time) {
    try {
      Path file = Paths.get("clock");
      BufferedWriter writer = Files.newBufferedWriter(file);
      System.out.println("lc~saving: " + time);
      writer.write(Integer.toString(time));
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * If the file 'clock' exists
   * - Restores the timestamp
   */
  private synchronized void restoreClock() {
    File f = new File("clock");
    if (f.exists() && f.length() != 0) {
      try {
        Path file = Paths.get("clock");
        BufferedReader reader = Files.newBufferedReader(file);
        setTime(Integer.parseInt(reader.readLine()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Gets current timestamp
   */
  public synchronized Integer getTime() {
    // return instance.timeStamp.get();
    restoreClock();
    return timeStamp;
  }

  /**
   * Sets new time stamp to provided integer.
   */
  public synchronized void setTime(Integer time) {
    timeStamp = time;
  }

  /**
   * Increments timestamp by 1
   */
  public synchronized void sendEvent() {
    setTime(getTime() + 1);
    System.out.println("lc~sendEvent - add 1: " + timeStamp);
    saveClock(timeStamp);
  }

  /**
   * Increments timestamp by max of:
   * - current timestamp
   * - and event time
   */
  public synchronized void receiveEvent(int eventTime) {
    setTime(Math.max(getTime(), eventTime) + 1);
    System.out.println(
      "lc~receiveEvent - add max: " + eventTime + "/" + timeStamp
    );
    saveClock(timeStamp);
  }
}
