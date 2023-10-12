package build;

/**
 * Request.java
 * Requests have:
 * - a clock (lamport clock time)
 * - a runnable thread
 * - and a csID (content server ID)
 *
 * They can also be compared with other Requests
 * To determine priority in a priority queue.
 */

public class Request implements Comparable<Request> {

  int clock;
  Thread thread;
  Integer threadID;
  String csID;

  Request(int clock, Thread thread, Integer threadID, String csID) {
    this.clock = clock;
    this.thread = thread;
    this.threadID = threadID;
    this.csID = csID;
  }

  @Override
  public int compareTo(Request other) {
    if (this.clock == other.clock) return 0; else if (
      this.clock > other.clock
    ) return 1; else return -1;
  }
}
