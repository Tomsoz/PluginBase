package xyz.tomsoz.pluginBase.common.database;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DatabaseHandler {
    default <T> List<T> getAll(Class<T> clazz) {
        return (List<T>) getDao(clazz).getAll();
    }

    default <T> Optional<T> get(Class<T> clazz, UUID id) {
        return (Optional<T>) getDao(clazz).get(id);
    }

    default <T> void save(Class<T> clazz, T t) {
        getDao(clazz).save(t);
    }

    default <T> void update(Class<T> clazz, T t, String[] params) {
        getDao(clazz).update(t, params);
    }

    default <T> void delete(Class<T> clazz, T t) {
        getDao(clazz).delete(t);
    }

    boolean isConnected();

    void connect();

    void destroy();

    void registerDaos();

    /**
     * Gets the DAO for a specific class.
     *
     * @param clazz The class to get the DAO for
     * @param <T>   The type of the class
     * @return The DAO for the specified class
     */
    <T> Dao<T> getDao(Class<?> clazz);

    default void wipeDatabase() {
        throw new UnsupportedOperationException();
    }

    List<SchemaUpgrader> getUpgraders();

    default void fixSchemas() {
        for (SchemaUpgrader upgrader : getUpgraders()) {
            if (upgrader.needsUpgrade()) upgrader.upgrade();
        }
    }
}
