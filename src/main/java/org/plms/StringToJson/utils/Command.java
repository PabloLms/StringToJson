package org.plms.StringToJson.utils;

import java.util.List;
import lombok.Getter;

@Getter
public class Command {

  private List<String> parameters;

  private TypeCommand typeCommand;

  public Command(final List<String> parameters, final TypeCommand typeCommand) {
    this.parameters = parameters;
    this.typeCommand = typeCommand;
  }
}
