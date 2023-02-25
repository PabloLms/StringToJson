package org.plms.StringToJson.utils;

import java.util.HashMap;
import java.util.Map;
import org.plms.StringToJson.tree.NodeReference;

public class Constants {

  public static final String PARENTHESIS_LEFT = "(";

  public static final String PARENTHESIS_RIGHT = ")";

  public static final String BRACKET_LEFT = "[";

  public static final String BRACKET_RIGHT = "]";
  /**
   * the regex is to find field=value, or fiel=,
   */
  public static final String REGEX_FIELD_VALUE = "([a-z]|[A-Z]|[0-9]){1,}\\={1}(([^((\\()(\\))(\\[)(\\])(\\=))]){0,}\\,){1,}";

  /**
   * the regex is to find field=value) ,value end in a object
   */
  public static final String REGEX_FIELD_VALUE_FINAL =
      "([a-z]|[A-Z]|[0-9]){1,}\\={1}(([^(\\()(\\[)(\\])(\\=)]){0,}\\)){1,}";

  /**
   * the regex is to find Object field=(object)
   */
  public static final String REGEX_FIELD_OBJECT
      = "([a-z]|[A-Z]|[1-9]){1,}\\=([a-z]|[A-Z]|[1-9]){1,}\\((\\w|\\-|\\,|\\s|\\$){1,}\\)";

  public static final String REGEX_FIELD_OBJECT_WITHOUT_NAME
      = "([a-z]|[A-Z]|[1-9]){1,}\\((\\w|\\-|\\,|\\s|\\$){1,}\\)";

  public static final String REGEX_FIELD_ARRAY = "([a-z]|[A-Z]){1,}\\=\\[(\\w|\\s|\\,|\\-|\\$){0,}\\]";

  public static final Map<String, NodeReference> ENCRYPTED_MAP = new HashMap<>();
}
