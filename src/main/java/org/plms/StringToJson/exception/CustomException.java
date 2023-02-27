package org.plms.StringToJson.exception;

import lombok.Getter;

@Getter
public class CustomException extends Exception {

  private Object object;

  public CustomException(final String message, final Object object) {
    super(message);
    this.object = object;
  }
}
