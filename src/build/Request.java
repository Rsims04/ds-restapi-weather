package build;

public class Request implements Comparable<Request> {

  int clock;
  Thread thread;
  String csID;

  Request(int clock, Thread thread, String csID) {
    this.clock = clock;
    this.thread = thread;
    this.csID = csID;
  }

  @Override
  public int compareTo(Request other) {
    if (this.clock == other.clock) return 0; else if (
      this.clock > other.clock
    ) return 1; else return -1;
  }
}
