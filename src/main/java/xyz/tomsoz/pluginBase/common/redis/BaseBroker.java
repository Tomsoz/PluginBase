package xyz.tomsoz.pluginBase.common.redis;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import lombok.Getter;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import xyz.tomsoz.pluginBase.extensions.BasePlugin;

import java.util.concurrent.TimeUnit;

public abstract class BaseBroker {
    @Getter
    private boolean connected = false;

    protected static final Object DUMMY_VALUE = new Object();

    protected final BasePlugin plugin;
    protected final Gson gson;
    protected final Cache<Integer, Object> cachedIds;

    protected BaseBroker(@NotNull BasePlugin plugin) {
        this.plugin = plugin;
        this.gson = GsonComponentSerializer.gson().serializer();
        this.cachedIds = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();
    }

    protected abstract void handle(@NotNull Message message);

    public abstract void connect();

    protected abstract void send(@NotNull Message message);

    public abstract void destroy();

    public void load() {
        plugin.getLogger().info("Connecting to REDIS...");
        connect();
        connected = true;
        plugin.getLogger().info("Successfully connected to REDIS!");
    }
}
