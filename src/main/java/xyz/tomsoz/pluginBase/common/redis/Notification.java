package xyz.tomsoz.pluginBase.common.redis;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class Notification {
    @Expose
    @Nullable
    private final UUID target;
    @Expose
    private final Component message;
}
