package build;

/**
 * LamportClock.java
 * [Description Here]
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

  public Integer getTime() {
    return timeStamp.get();
  }

  public void setTime(Integer time) {
    timeStamp.set(time);
  }

  // Increments time by 1
  public synchronized void sendEvent() {
    timeStamp.incrementAndGet();
    System.out.println("sendEvent - add 1: " + timeStamp);
    saveClock();
  }

  public synchronized void receiveEvent(int eventTime) {
    timeStamp.set(Math.max(getTime(), eventTime) + 1);
    System.out.println("receiveEvent - add max: " + timeStamp);
    saveClock();
  }
}
