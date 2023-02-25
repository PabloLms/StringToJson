package org.plms.StringToJson.utils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.plms.StringToJson.tree.NodeReference;
import org.plms.StringToJson.tree.Type;

public class StringToJsonUtils {

  public static boolean isObjectInit(final String text) {
    return comparativeStartEnd(text, Constants.BRACKET_LEFT, Constants.BRACKET_RIGHT);
  }

  public static boolean isArrayInit(final String text) {
    return comparativeStartEnd(text, Constants.PARENTHESIS_LEFT, Constants.PARENTHESIS_RIGHT);
  }

  private static boolean comparativeStartEnd(final String text, final String characterStart,
      final String characterEnd) {
    final String[] arrayString = StringUtils.split(text);
    return arrayString[0].equals(characterStart) && arrayString[arrayString.length - 1].equals(
        characterEnd);
  }

  private static String replaceEncrypted(String text, final String[] replaces, final Type type) {
    for (final String replaced : replaces) {
      final UUID uuid = UUID.randomUUID();
      Constants.ENCRYPTED_MAP.put(uuid.toString(), new NodeReference(replaced, type));
      text = StringUtils.replace(text, replaced, StringUtils.join(uuid, "$"));
    }
    return text;
  }

  private static String replaceEncryptedFieldValue(String data, String regex) {
    Matcher matcher = Pattern.compile(regex).matcher(data);
    while (matcher.find()) {
      final UUID uuid = UUID.randomUUID();
      String replaced = matcher.group();
      String beforeText = StringUtils.substring(data, 0, matcher.start());
      String afterText = StringUtils.substring(data, matcher.start(), data.length());
      replaced = StringUtils.substring(replaced, 0, replaced.length() - 1);
      Constants.ENCRYPTED_MAP.put(uuid.toString(), new NodeReference(replaced, Type.FIELD_VALUE));
      data = StringUtils.join(beforeText,
          StringUtils.replaceOnce(afterText, replaced, StringUtils.join(uuid, "$")));
      matcher = Pattern.compile(regex).matcher(data);
    }
    return data;
  }

  public static String findRegexFieldValue(final String data) {
    return replaceEncryptedFieldValue(data, Constants.REGEX_FIELD_VALUE);
  }

  public static String findRegexFieldValueFinal(final String data) {
    return replaceEncryptedFieldValue(data, Constants.REGEX_FIELD_VALUE_FINAL);
  }

  public static String findObjectsAndArray(String data) {
    boolean anySearch = true;
    while (anySearch) {
      anySearch = false;
      while (Pattern.compile(Constants.REGEX_FIELD_ARRAY).matcher(data).find()) {
        data = findRegex(data, Type.ARRAY_VALUE, Constants.REGEX_FIELD_ARRAY);
        anySearch = true;
      }
      while (Pattern.compile(Constants.REGEX_FIELD_OBJECT).matcher(data).find()) {
        data = findRegex(data, Type.OBJECT_VALUE, Constants.REGEX_FIELD_OBJECT);
        anySearch = true;
      }
      while (Pattern.compile(Constants.REGEX_FIELD_OBJECT_WITHOUT_NAME).matcher(data).find()) {
        data = findRegex(data, Type.OBJECT_VALUE_WITHOUT_NAME,
            Constants.REGEX_FIELD_OBJECT_WITHOUT_NAME);
        anySearch = true;
      }
    }
    return data;
  }

  private static String findRegex(final String data, final Type type, final String regex) {
    return replaceEncrypted(data, regexDefault(data, regex).toArray(String[]::new), type);
  }

  private static Stream<String> regexDefault(final String data, final String regex) {
    return Pattern.compile(regex).matcher(data).results().map(MatchResult::group).distinct();
  }

  public static List<String> caseChildren(final String group, final String charStart,
      final String charEnd) {
    return Arrays.stream(StringUtils.split(
            StringUtils.substring(group, StringUtils.indexOf(group, charStart) + 1,
                StringUtils.lastIndexOf(group, charEnd)), ","))
        .map(element -> StringUtils.stripStart(element, null)).collect(
            Collectors.toList());
  }
}