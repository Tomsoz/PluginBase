package xyz.tomsoz.pluginBase.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CommandArgument {
    String name;
    String description;
    Class<?> type;
}
