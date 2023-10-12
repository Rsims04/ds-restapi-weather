package build;

/**
 * JSONParser.java
 * Custom JSON Parser:
 * - Converts from File to JSON.
 * - Converts from JSON to String.
 * - Validates JSON objects.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Stack;

public class JSONParser {

  /**
   * Checks whether string:
   * - is float.
   * - is integer.
   * - else is string.
   */
  public String isNumber(String string) {
    if (string.contains(".")) {
      try {
        Float.parseFloat(string);
        return "float";
      } catch (NumberFormatException ex) {
        return "string";
      }
    }
    try {
      Integer.parseInt(string);
      return "int";
    } catch (NumberFormatException ex) {
      return "string";
    }
  }

  /**
   * Reads from file:
   * - Formats into JSON objects.
   * - and returns JSON object array
   */
  public ArrayList<String> toJSON(File file) throws IOException {
    ArrayList<String> jsonStringArray = new ArrayList<String>();
    if (file.length() <= 0) {
      jsonStringArray.add("");
      return jsonStringArray;
    }
    String jsonObject;
    try (
      BufferedReader bufferedReader = Files.newBufferedReader(file.toPath())
    ) {
      jsonObject = "{";
      String line = bufferedReader.readLine();
      while (line != null) {
        if (line.isEmpty()) {
          // Remove trailing comma
          jsonObject = jsonObject.substring(0, jsonObject.length() - 1);
          jsonObject += "\n" + "}";
          jsonStringArray.add(jsonObject);
          line = bufferedReader.readLine();
          if (line == null) {
            break;
          } else {
            jsonObject = "{";
          }
        }
        String[] keyValPair = line.split(":", 2);
        String key = "\"" + keyValPair[0].trim() + "\"";
        Object value = new Object();
        if (!keyValPair[0].contains("date")) {
          switch (isNumber(keyValPair[1])) {
            case ("int"):
              value = Integer.parseInt(keyValPair[1]);
              break;
            case ("float"):
              value = Float.parseFloat(keyValPair[1]);
              break;
            default:
              value = String.valueOf("\"" + keyValPair[1].trim() + "\"");
              break;
          }
        } else {
          value = String.valueOf("\"" + keyValPair[1].trim() + "\"");
        }
        jsonObject += "\n\t" + key + " : " + value + ",";
        line = bufferedReader.readLine();
      }
      // Remove trailing comma
      jsonObject = jsonObject.substring(0, jsonObject.length() - 1);
      jsonObject += "\n" + "}";
      jsonStringArray.add(jsonObject);
    }
    return jsonStringArray;
  }

  /**
   * Read from JSON object:
   * - Strips JSON formatting.
   * - and converts to text.
   */
  public String fromJSON(String jsonObject) throws IOException {
    String jsonText = "";
    BufferedReader br = new BufferedReader(new StringReader(jsonObject));
    String line = br.readLine();

    if (line != null) {
      while (!line.equals("{")) {
        line = br.readLine();
      }
      while ((line = br.readLine()) != null) {
        if (line.equals("}")) {
          break;
        }

        String keyValPair[] = line.split(":", 2);
        if (keyValPair.length > 1) {
          String key = keyValPair[0].replace("\"", "").trim();
          String value = keyValPair[1].replace(",", "").trim();
          value = value.replace("\"", "");

          jsonText += key + ":" + value + "\n";
        }
      }
    }
    return jsonText;
  }

  /**
   * Parses JSON and returns:
   * - True if valid.
   * - False if invalid.
   */
  public boolean validateJSON(String jsonObject) throws IOException {
    if (jsonObject.length() <= 0) {
      return false;
    }
    Stack<Character> stack = new Stack<>();
    boolean endFlag = false;
    Character previous;
    for (Character token : jsonObject.toCharArray()) {
      switch (token) {
        case '{':
          if (stack.empty()) {
            stack.push('{');
            break;
          } else {
            return false;
          }
        case '\"':
          if (stack.empty()) {
            return false;
          }
          previous = stack.peek();
          if (previous != '\"') {
            stack.push('\"');
            break;
          } else {
            stack.pop();
            break;
          }
        case ',':
        case ':':
          previous = stack.peek();
          if (previous == '\"') {
            break;
          }
          if (previous != '{') {
            return false;
          } else {
            break;
          }
        case '}':
          if (stack.isEmpty()) {
            return false;
          }
          endFlag = true;
          previous = stack.pop();
          if (previous != '{') {
            return false;
          } else {
            break;
          }
        default:
          if (stack.empty()) {
            return false;
          }
      }
      if (stack.isEmpty() && endFlag == true) {
        return true;
      }
    }
    return false;
  }

  public static void main(String[] args) throws IOException {
    JSONParser j = new JSONParser();
    // File f = new File("input.txt");
    File f = new File("input.txt");
    // String jo = "PUT \n";
    // jo += "content-type: json\n";
    // jo += "content-length: 100 {\n";
    // jo += j.toJSON(f);

    String jo = j.toJSON(f).get(0);
    System.out.println(jo);

    System.out.println(j.validateJSON(jo));
    // LinkedHashMap<String, String> lhm = j.fromJSON(jo);
    String lhm = j.fromJSON(jo);
    System.out.println(lhm);
    // System.out.println(lhm.values());
    // System.out.println(lhm.get("name"));
    // System.out.println(lhm.get("id"));
  }
}
