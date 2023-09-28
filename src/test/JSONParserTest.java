package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import build.JSONParser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JSONParserTest {

  JSONParser jp = new JSONParser();

  @Test
  public void is_number_string_int_float() {
    String testVal = "weather";
    String expected = "string";
    assertEquals(jp.isNumber(testVal), expected);

    testVal = "4";
    expected = "int";
    assertEquals(jp.isNumber(testVal), expected);

    testVal = "3.5";
    expected = "float";
    assertEquals(jp.isNumber(testVal), expected);

    testVal = "0";
    expected = "int";
    assertEquals(jp.isNumber(testVal), expected);

    testVal = "hello.";
    expected = "string";
    assertEquals(jp.isNumber(testVal), expected);
  }

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void to_json() throws IOException {
    File file = folder.newFile("testFile.txt");
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      writer.write(
        "id:IDS60901\n" +
        "name:Adelaide (West Terrace /  ngayirdapira)\n" +
        "state: SA\n" +
        "time_zone:CST\n" +
        "lat:-34.9\n" +
        "lon:138.6\n" +
        "local_date_time:15/04:00pm\n" +
        "local_date_time_full:20230715160000\n" +
        "air_temp:13.3\n" +
        "apparent_t:9.5\n" +
        "cloud:Partly cloudy\n" +
        "dewpt:5.7\n" +
        "press:1023.9\n" +
        "rel_hum:60\n" +
        "wind_dir:S\n" +
        "wind_spd_kmh:15\n" +
        "wind_spd_kt:8\n"
      );
    }
    String expected =
      "{\n" +
      "\t\"id\" : \"IDS60901\",\n" +
      "\t\"name\" : \"Adelaide (West Terrace /  ngayirdapira)\",\n" +
      "\t\"state\" : \"SA\",\n" +
      "\t\"time_zone\" : \"CST\",\n" +
      "\t\"lat\" : -34.9,\n" +
      "\t\"lon\" : 138.6,\n" +
      "\t\"local_date_time\" : \"15/04:00pm\",\n" +
      "\t\"local_date_time_full\" : \"20230715160000\",\n" +
      "\t\"air_temp\" : 13.3,\n" +
      "\t\"apparent_t\" : 9.5,\n" +
      "\t\"cloud\" : \"Partly cloudy\",\n" +
      "\t\"dewpt\" : 5.7,\n" +
      "\t\"press\" : 1023.9,\n" +
      "\t\"rel_hum\" : 60,\n" +
      "\t\"wind_dir\" : \"S\",\n" +
      "\t\"wind_spd_kmh\" : 15,\n" +
      "\t\"wind_spd_kt\" : 8\n" +
      "}";
    ArrayList<String> testStringArray = jp.toJSON(file);
    String testString = testStringArray.get(0);

    assertEquals(expected, testString);
  }

  @Test
  public void from_json() throws IOException {
    String jsonObject =
      "{\n" +
      "\t\"id\" : \"IDS60901\",\n" +
      "\t\"name\" : \"Adelaide (West Terrace /  ngayirdapira)\",\n" +
      "\t\"state\" : \"SA\",\n" +
      "\t\"time_zone\" : \"CST\",\n" +
      "\t\"lat\" : -34.9,\n" +
      "\t\"lon\" : 138.6,\n" +
      "\t\"local_date_time\" : \"15/04:00pm\",\n" +
      "\t\"local_date_time_full\" : \"20230715160000\",\n" +
      "\t\"air_temp\" : 13.3,\n" +
      "\t\"apparent_t\" : 9.5,\n" +
      "\t\"cloud\" : \"Partly cloudy\",\n" +
      "\t\"dewpt\" : 5.7,\n" +
      "\t\"press\" : 1023.9,\n" +
      "\t\"rel_hum\" : 60,\n" +
      "\t\"wind_dir\" : \"S\",\n" +
      "\t\"wind_spd_kmh\" : 15,\n" +
      "\t\"wind_spd_kt\" : 8\n" +
      "}";

    String expected =
      "id:IDS60901\n" +
      "name:Adelaide (West Terrace /  ngayirdapira)\n" +
      "state:SA\n" +
      "time_zone:CST\n" +
      "lat:-34.9\n" +
      "lon:138.6\n" +
      "local_date_time:15/04:00pm\n" +
      "local_date_time_full:20230715160000\n" +
      "air_temp:13.3\n" +
      "apparent_t:9.5\n" +
      "cloud:Partly cloudy\n" +
      "dewpt:5.7\n" +
      "press:1023.9\n" +
      "rel_hum:60\n" +
      "wind_dir:S\n" +
      "wind_spd_kmh:15\n" +
      "wind_spd_kt:8\n";

    String testString = jp.fromJSON(jsonObject);

    assertEquals(expected, testString);
  }

  @Test
  public void validate_json() throws IOException {
    String jsonObject =
      "{\n" +
      "\t\"id\" : \"IDS60901\",\n" +
      "\t\"name\" : \"Adelaide (West Terrace /  ngayirdapira)\",\n" +
      "\t\"state\" : \"SA\",\n" +
      "\t\"time_zone\" : \"CST\",\n" +
      "\t\"lat\" : -34.9,\n" +
      "\t\"lon\" : 138.6,\n" +
      "\t\"local_date_time\" : \"15/04:00pm\",\n" +
      "\t\"local_date_time_full\" : \"20230715160000\",\n" +
      "\t\"air_temp\" : 13.3,\n" +
      "\t\"apparent_t\" : 9.5,\n" +
      "\t\"cloud\" : \"Partly cloudy\",\n" +
      "\t\"dewpt\" : 5.7,\n" +
      "\t\"press\" : 1023.9,\n" +
      "\t\"rel_hum\" : 60,\n" +
      "\t\"wind_dir\" : \"S\",\n" +
      "\t\"wind_spd_kmh\" : 15,\n" +
      "\t\"wind_spd_kt\" : 8\n" +
      "}";

    Boolean testBool = jp.validateJSON(jsonObject);

    assertEquals(true, testBool);
  }
}
