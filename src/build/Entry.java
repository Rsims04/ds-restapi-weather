package build;

/**
 * Entry.java
 * An Entry stores:
 * - a jsonObject
 * - and a date
 *
 * Used in local storage.
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
