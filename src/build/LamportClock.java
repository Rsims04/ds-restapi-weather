/**
 * GETClient.java
 * [Description Here]
 */
package build;

import java.util.concurrent.atomic.AtomicInteger;

public class LamportClock {

  private AtomicInteger timeStamp = new AtomicInteger(0); // To be incremented

  public Integer getTime() {
    return timeStamp.get();
  }

  // Increments time by 1
  public synchronized void sendEvent() {
    timeStamp.incrementAndGet();
    System.out.println("sendEvent - add 1: " + timeStamp);
  }

  public synchronized void receiveEvent(int eventTime) {
    timeStamp.set(Math.max(getTime(), eventTime) + 1);
    System.out.println("receiveEvent - add: " + timeStamp);
  }
}
