package xyz.tomsoz.pluginBase.common.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.tomsoz.pluginBase.common.redis.BaseBroker;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A base class which communicates with cache layers
 * All you need to do is implement the addCaches function and call registerCache
 */
public abstract class BaseCacheAccess {
    private static final Map<Class<?>, Cache<?>> cacheMap = new ConcurrentHashMap<>();

    static {
        addCaches();
    }

    protected static void addCaches() {
    }

    protected static <T> void registerCache(Class<T> clazz, @Nullable BaseBroker redisBroker, CacheFactory<T> memoryCache, CacheFactory<T> distributedCache) {
        Cache<T> cache;
        if (redisBroker != null && redisBroker.isConnected()) {
            cache = distributedCache.create();
        } else {
            cache = memoryCache.create();
        }
        cacheMap.put(clazz, cache);
    }

    @SuppressWarnings("unchecked")
    private static <T> Cache<T> getCacheForClass(Class<T> clazz) {
        Cache<?> cache = cacheMap.get(clazz);
        if (cache == null) {
            throw new RuntimeException("No cache found for class '%s'".formatted(clazz.getName()));
        }
        return (Cache<T>) cache;
    }

    public static <T> void add(Class<T> clazz, @Nullable T obj) {
        getCacheForClass(clazz).add(obj);
    }

    public static <T> Optional<T> get(Class<T> clazz, UUID uuid) {
        return Optional.ofNullable(getCacheForClass(clazz).get(uuid));
    }

    public static <T> @Nullable T getNullable(Class<T> clazz, UUID uuid) {
        return getCacheForClass(clazz).get(uuid);
    }

    public static <T> @NotNull T getNotNull(Class<T> clazz, UUID uuid) {
        var cached = getCacheForClass(clazz).get(uuid);
        if (cached != null) {
            return cached;
        }
        throw new NullPointerException("No cached item found for class %s with identifier %s".formatted(clazz.getName(), uuid));
    }

    public static <T> void invalidate(Class<T> clazz, @NotNull UUID uuid) {
        getCacheForClass(clazz).invalidate(uuid);
    }

    public static <T> void invalidate(Class<T> clazz, @NotNull T obj) {
        getCacheForClass(clazz).invalidate(obj);
    }

    public static <T> @NotNull List<T> getAll(Class<T> clazz) {
        return getCacheForClass(clazz).getAll();
    }

    public static <T> int size(Class<T> clazz) {
        return getCacheForClass(clazz).size();
    }

    @FunctionalInterface
    protected interface CacheFactory<T> {
        Cache<T> create();
    }
}
