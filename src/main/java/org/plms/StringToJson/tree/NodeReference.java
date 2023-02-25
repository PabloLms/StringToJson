package org.plms.StringToJson.tree;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.plms.StringToJson.utils.Constants;
import org.plms.StringToJson.utils.StringToJsonUtils;

@Builder
@AllArgsConstructor
@Data
public class NodeReference {

  private String key;

  private String value;

  private String group;

  private Type type;

  private List<String> children;

  public NodeReference(final String group, final Type type) {
    this.type = type;
    this.group = group;
    children = new ArrayList<>();
    this.convert();
  }

  private void convert() {
    if (Type.FIELD_VALUE.equals(type)) {
      this.caseKeyValue();
    } else if (Type.OBJECT_VALUE.equals(type)) {
      this.caseKeyValue();
      this.children = StringToJsonUtils.caseChildren(this.group, Constants.PARENTHESIS_LEFT,
          Constants.PARENTHESIS_RIGHT);
    } else if (Type.OBJECT_VALUE_WITHOUT_NAME.equals(type)) {
      this.children = StringToJsonUtils.caseChildren(this.group, Constants.PARENTHESIS_LEFT,
          Constants.PARENTHESIS_RIGHT);
    } else {
      this.caseKeyValue();
      this.children = StringToJsonUtils.caseChildren(this.group, Constants.BRACKET_LEFT,
          Constants.BRACKET_RIGHT);
    }
  }

  private void caseKeyValue() {
    int indexSymbolEqual =
        StringUtils.indexOf(this.group, "=");
    this.key = StringUtils.substring(this.group, 0, indexSymbolEqual);
    this.value = StringUtils.substring(this.group, indexSymbolEqual + 1);
  }
}
