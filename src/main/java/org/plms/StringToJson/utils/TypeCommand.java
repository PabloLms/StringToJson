package org.plms.StringToJson.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TypeCommand {
  OBJECT_NOT_NULL("Not show values null in case of be a object"),
  OBJECT_NULL("Show values null in case of be a object"),
  ARRAY_NOT_NULL("Not show values null in case of be a array"),
  ARRAY_NULL("Show values null in case of be a array");
  private final String description;
}
