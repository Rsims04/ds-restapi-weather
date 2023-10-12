package build;

import java.time.LocalDateTime;

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
  LocalDateTime date;

  Request(
    int clock,
    LocalDateTime date,
    Thread thread,
    Integer threadID,
    String csID
  ) {
    this.clock = clock;
    this.thread = thread;
    this.threadID = threadID;
    this.csID = csID;
    this.date = date;
  }

  /**
   * Determines order in the priority Queue.
   * By clock time.
   * 1 for higher.
   * -1 for lower.
   * 0 for neutral.
   */
  @Override
  public int compareTo(Request other) {
    if (this.clock == other.clock) {
      if (this.date.isBefore(other.date)) {
        return 1;
      } else {
        return 0;
      }
    } else if (this.clock > other.clock) {
      return 1;
    } else {
      return -1;
    }
  }
}
