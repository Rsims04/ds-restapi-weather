package build;

/**
 * Entry.java
 * [Description Here]
 */
import java.time.LocalDateTime;

public class Entry {

  String jsonObject;
  LocalDateTime date;

  Entry(String jsonObject, LocalDateTime date) {
    this.jsonObject = jsonObject;
    this.date = date;
  }
}
