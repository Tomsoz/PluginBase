package xyz.tomsoz.pluginBase.common.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BrokerType {
    REDIS("Redis"),
    ;
    private final String displayName;
}
