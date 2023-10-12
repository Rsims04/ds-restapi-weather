package build;

/**
 * Entry.java
 * An Entry stores:
 * - a jsonObject
 * - and a date
 *
 * Used in local storage to compare entry dates.
 * While storing the JSON data.
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
