package build;

/**
 * JSONParser.java
 * [Description Here]
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Stack;

public class JSONParser {

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
          System.out.println(jsonObject);
          line = bufferedReader.readLine();
          if (line == null) {
            System.out.println("NULL ALERT!!!");
            break;
          } else {
            jsonObject = "{";
            System.out.println(line);
          }
        }
        String[] keyValPair = line.split(":");
        String key = "\"" + keyValPair[0].strip() + "\"";
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
              value = String.valueOf("\"" + keyValPair[1].strip() + "\"");
              break;
          }
        } else {
          value = String.valueOf("\"" + keyValPair[1].strip() + "\"");
        }
        jsonObject += "\n\t" + key + " : " + value + ",";
        line = bufferedReader.readLine();
        // System.out.println("AFTER: " + line);
      }
      // Remove trailing comma
      jsonObject = jsonObject.substring(0, jsonObject.length() - 1);
      jsonObject += "\n" + "}";
      jsonStringArray.add(jsonObject);
      System.out.println(jsonObject);
    }
    return jsonStringArray;
  }

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

        String keyValPair[] = line.split(":");
        String key = keyValPair[0].replace("\"", "").strip();
        String value = keyValPair[1].replace(",", "").strip();
        value = value.replace("\"", "");

        jsonText += key + ":" + value + "\n";
      }
      jsonText = jsonText.substring(0, jsonText.length() - 1);
    }
    return jsonText;
  }

  public boolean validateJSON(String jsonObject) throws IOException {
    if (jsonObject.length() <= 0) {
      return false;
    }
    Stack<Character> stack = new Stack<>();
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
          Character previous = stack.peek();
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
          if (previous != '{') {
            return false;
          } else {
            break;
          }
        case '}':
          if (stack.isEmpty()) {
            return false;
          }
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
      if (stack.isEmpty()) {
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

    // System.out.println(j.validateJSON(jo));
    // LinkedHashMap<String, String> lhm = j.fromJSON(jo);
    String lhm = j.fromJSON(jo);
    System.out.println(lhm);
    // System.out.println(lhm.values());
    // System.out.println(lhm.get("name"));
    // System.out.println(lhm.get("id"));
  }
}
