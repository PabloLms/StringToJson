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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.plms.StringToJson.exception.CustomException;
import org.plms.StringToJson.tree.NodeReference;
import org.plms.StringToJson.tree.Type;
import org.plms.StringToJson.utils.Command;
import org.plms.StringToJson.utils.Constants;
import org.plms.StringToJson.utils.StringToJsonUtils;
import org.plms.StringToJson.utils.TypeCommand;

public class App {

  private static boolean withNull = true;

  public static void main(final String[] args) {
    if (args.length == 2 || args.length == 3) {
      final Optional<TypeCommand> optionalTypeCommand = StringToJsonUtils.searchCommand(args);
      if (optionalTypeCommand.isPresent()) {
        final String path = args[args.length - 1];
        Path of = Path.of(path);
        if (Files.exists(of)) {
          final TypeCommand typeCommand = optionalTypeCommand.get();
          try (Stream<String> stream = Files.lines(of, StandardCharsets.UTF_8)) {
            String data = stream.collect(Collectors.toList()).get(0);
            data = StringToJsonUtils.findRegexFieldValue(data);
            data = StringToJsonUtils.findRegexFieldValueFinal(data);
            data = StringToJsonUtils.findObjectsAndArray(data);
            Object object = null;
            File file = new File(".\\json_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + ".json");
            try {
              System.out.println("Command: " + typeCommand.name());
              switch (typeCommand) {
                case ARRAY_NULL:
                  object = routeJsonArray(new JsonArray(),
                      StringToJsonUtils.caseChildren(data, Constants.BRACKET_LEFT,
                          Constants.BRACKET_RIGHT), 0);
                  break;
                case OBJECT_NULL:
                  object = routeJsonObject(new JsonObject(),
                      StringToJsonUtils.caseChildren(data, Constants.PARENTHESIS_LEFT,
                          Constants.PARENTHESIS_RIGHT), 0);
                  break;
                case ARRAY_NOT_NULL:
                  withNull = false;
                  object = routeJsonArray(new JsonArray(),
                      StringToJsonUtils.caseChildren(data, Constants.BRACKET_LEFT,
                          Constants.BRACKET_RIGHT), 0);
                  break;
                case OBJECT_NOT_NULL:
                  withNull = false;
                  object = routeJsonObject(new JsonObject(),
                      StringToJsonUtils.caseChildren(data, Constants.PARENTHESIS_LEFT,
                          Constants.PARENTHESIS_RIGHT), 0);
                  break;
              }
            } catch (CustomException e) {
              object = e.getObject();
            }
            if (Objects.nonNull(object)) {
              Gson gson = new GsonBuilder().setPrettyPrinting().create();
              JsonElement jsonElement = JsonParser.parseString(object.toString());
              try (FileWriter fileWriter = new FileWriter(file, true)) {
                fileWriter.append(gson.toJson(jsonElement));
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        } else {
          System.out.println("File not found");
        }
      } else {
        System.out.println("Command not found");
      }
    } else {
      final int max = Constants.commands.stream().map(command -> String.join(StringUtils.SPACE,
              String.join(StringUtils.SPACE, command.getParameters()))).map(String::length)
          .max(Integer::compareTo).orElse(0);
      for (Command command : Constants.commands) {
        String message = String.join(StringUtils.SPACE,
            String.join(StringUtils.SPACE, command.getParameters()));
        message = StringUtils.rightPad(message, max, StringUtils.SPACE);
        System.out.println(StringUtils.join("   ", message, "| Description:",
            command.getTypeCommand().getDescription()));
      }
    }
  }

  public static JsonObject routeJsonObject(final JsonObject jsonObject, final List<String> children,
      final int position) throws CustomException {
    if (children.size() == position) {
      return jsonObject;
    } else {
      NodeReference nodeReference = retrieveNodeReference(children.get(position), jsonObject);
      if (nodeReference.getType().equals(Type.FIELD_VALUE)) {
        if (!nodeReference.getValue().equalsIgnoreCase("null") && !StringUtils.isEmpty(
            nodeReference.getValue())) {
          jsonObject.add(nodeReference.getKey(), new JsonPrimitive(nodeReference.getValue()));
        } else if (withNull) {
          jsonObject.add(nodeReference.getKey(), new JsonPrimitive(nodeReference.getValue()));
        }
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
        if (!nodeReference.getValue().equalsIgnoreCase("null") && !StringUtils.isEmpty(
            nodeReference.getValue())) {
          jsonArray.add(new JsonPrimitive(nodeReference.getValue()));
        } else if (withNull) {
          jsonArray.add(new JsonPrimitive(nodeReference.getValue()));
        }
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
    throw new CustomException("Ocurrió un error - key:" + key, object);
  }
}
