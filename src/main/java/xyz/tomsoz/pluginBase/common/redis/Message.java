package xyz.tomsoz.pluginBase.common.redis;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.concurrent.ThreadLocalRandom;

@Getter
@AllArgsConstructor
@Builder
public class Message {
    @Expose
    @Builder.Default
    private Integer id = ThreadLocalRandom.current().nextInt(0, 999999 + 1);
    @Expose
    private Type type;
    @Expose
    private Payload payload;

    public void send(BaseBroker broker) {
        if (broker == null) return;
        broker.cachedIds.put(id, BaseBroker.DUMMY_VALUE);
        broker.send(this);
    }

    public enum Type {
        NOTIFICATION,
        RELOAD,
        TOGGLE
    }
}
