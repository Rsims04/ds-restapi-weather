package build;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.LinkedHashMap;

public class JSONParser {

  public String toJSON(File file) throws IOException {
    String jsonObject = "{";
    try (
      BufferedReader bufferedReader = Files.newBufferedReader(file.toPath())
    ) {
      String line = bufferedReader.readLine();
      while (line != null) {
        String[] keyValPair = line.split(":");
        String key = "\"" + keyValPair[0] + "\"";
        String value = keyValPair[1];
        jsonObject += "\n\t" + key + " : " + value + ",";

        line = bufferedReader.readLine();
      }
    }
    // Remove trailing comma
    jsonObject = jsonObject.substring(0, jsonObject.length() - 1);
    jsonObject += "\n" + "}";

    return jsonObject;
  }

  public LinkedHashMap<String, String> fromJSON(String jsonObject)
    throws IOException {
    LinkedHashMap<String, String> jsonHashMap = new LinkedHashMap<String, String>();
    BufferedReader br = new BufferedReader(new StringReader(jsonObject));
    String line;

    br.readLine();
    while ((line = br.readLine()) != null) {
      if (line.equals("}")) {
        break;
      }

      String keyValPair[] = line.split(":");
      String key = keyValPair[0].replace("\"", "").strip();
      String value = keyValPair[1].replace(",", "").strip();

      jsonHashMap.put(key, value);
    }

    return jsonHashMap;
  }
  //   public static void main(String[] args) throws IOException {
  //     // JSONParser j = new JSONParser();
  //     // File f = new File("input.txt");
  //     // String jo = j.toJSON(f);
  //     // LinkedHashMap<String, String> lhm = j.fromJSON(jo);
  //     // System.out.println(lhm.get("name"));
  //   }
}
