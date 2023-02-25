package org.plms.StringToJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.plms.StringToJson.exception.CustomException;
import org.plms.StringToJson.tree.NodeReference;
import org.plms.StringToJson.tree.Type;
import org.plms.StringToJson.utils.Constants;
import org.plms.StringToJson.utils.StringToJsonUtils;

public class App {

  public static void main(final String[] args) {
    if (args.length == 2) {
      final String type = args[0];
      final String path = args[1];
      final Path of = Path.of(path);
      if (Files.exists(of) && (type.equalsIgnoreCase("-Object") || type.equalsIgnoreCase(
          "-Array"))) {
        try (Stream<String> stream = Files.lines(of, StandardCharsets.UTF_8)) {
          String data = stream.collect(Collectors.toList()).get(0);
          data = StringToJsonUtils.findRegexFieldValue(data);
          data = StringToJsonUtils.findRegexFieldValueFinal(data);
          data = StringToJsonUtils.findObjectsAndArray(data);
          File file = new File(".\\json_" + LocalDateTime.now()
              .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + ".json");
          try (FileWriter fileWriter = new FileWriter(file, true)) {
            if (type.equalsIgnoreCase("-Object")) {
              JsonObject jsonObject = new JsonObject();
              routeJsonObject(jsonObject,
                  StringToJsonUtils.caseChildren(data, Constants.PARENTHESIS_LEFT,
                      Constants.PARENTHESIS_RIGHT), 0);
              System.out.println("Completed Success");
              Gson gson = new GsonBuilder().setPrettyPrinting().create();
              JsonElement el = JsonParser.parseString(jsonObject.toString());
              fileWriter.append(gson.toJson(el));
            } else if (type.equalsIgnoreCase("-Array")) {
              JsonArray jsonArray = new JsonArray();
              routeJsonArray(jsonArray, StringToJsonUtils.caseChildren(data, Constants.BRACKET_LEFT,
                  Constants.BRACKET_RIGHT), 0);
              System.out.println("Completed Success");
              Gson gson = new GsonBuilder().setPrettyPrinting().create();
              JsonElement el = JsonParser.parseString(jsonArray.toString());
              fileWriter.append(gson.toJson(el));
            } else {
              System.out.println("type incorrect");
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        } catch (IOException io) {
          io.printStackTrace();
        }
      } else {
        System.out.println("file no exits");
      }
    } else {
      System.out.println("          Commands basics");
      System.out.println("  -Object file.txt");
//      System.out.println("  --Array file.txt");
    }
  }

  public static JsonObject routeJsonObject(final JsonObject jsonObject, final List<String> children,
      final int position) throws CustomException {
    if (children.size() == position) {
      return jsonObject;
    } else {
      NodeReference nodeReference = retrieveNodeReference(children.get(position), jsonObject);
      if (nodeReference.getType().equals(Type.FIELD_VALUE)) {
        jsonObject.add(nodeReference.getKey(), new JsonPrimitive(nodeReference.getValue()));
      } else if (nodeReference.getType().equals(Type.OBJECT_VALUE)) {
        jsonObject.add(nodeReference.getKey(),
            routeJsonObject(new JsonObject(), nodeReference.getChildren(), 0));
      } else if (nodeReference.getType().equals(Type.ARRAY_VALUE)) {
        jsonObject.add(nodeReference.getKey(),
            routeJsonArray(new JsonArray(), nodeReference.getChildren(), 0));
      }
      return routeJsonObject(jsonObject, children, position + 1);
    }
  }

  public static JsonArray routeJsonArray(final JsonArray jsonArray, final List<String> children,
      final int position) throws CustomException {
    if (children.size() == position) {
      return jsonArray;
    } else {
      NodeReference nodeReference = retrieveNodeReferenceOtherCase(children.get(position),
          jsonArray);
      if (nodeReference.getType().equals(Type.VALUE_PRIMITIVE)) {
        jsonArray.add(new JsonPrimitive(nodeReference.getValue()));
      } else if (nodeReference.getType().equals(Type.OBJECT_VALUE_WITHOUT_NAME)) {
        jsonArray.add(routeJsonObject(new JsonObject(), nodeReference.getChildren(), 0));
      }
      return routeJsonArray(jsonArray, children, position + 1);
    }
  }

  public static NodeReference retrieveNodeReferenceOtherCase(final String key, final Object object)
      throws CustomException {
    int indexEnd = StringUtils.indexOf(key, "$");
    if (indexEnd == -1) {
      return NodeReference.builder().value(key).type(Type.VALUE_PRIMITIVE).build();
    } else {
      return retrieveNodeReference(key, object);
    }
  }

  public static NodeReference retrieveNodeReference(final String key, final Object object)
      throws CustomException {
    int indexEnd = StringUtils.indexOf(key, "$");
    NodeReference nodeReference = Constants.ENCRYPTED_MAP.get(
        StringUtils.substring(key, 0, indexEnd));
    if (nodeReference != null) {
      nodeReference.setValue(nodeReference.getValue() + StringUtils.substring(key, indexEnd + 1));
      return nodeReference;
    }
    throw new CustomException("Ocurrió un error - key:" + key + "Object:" + object);
  }
}
