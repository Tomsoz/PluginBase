package xyz.tomsoz.pluginBase.common.database;

import xyz.tomsoz.pluginBase.common.Tuple;
import xyz.tomsoz.pluginBase.extensions.BasePlugin;
import xyz.tomsoz.pluginBase.common.flavor.annotations.Close;
import xyz.tomsoz.pluginBase.common.flavor.annotations.Configure;
import xyz.tomsoz.pluginBase.common.flavor.annotations.inject.Inject;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * This is a base for a manager for all database interactions.
 */
public abstract class BaseDataService {
    public Logger logger;
    public BasePlugin plugin;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    protected final Map<DatabaseType, Class<? extends DatabaseHandler>> databaseHandlers = new HashMap<>();
    protected DatabaseHandler handler;

    protected void configure(Logger logger, BasePlugin plugin) {
        this.logger = logger;
        this.plugin = plugin;
        handler = initHandler();
        handler.connect();
    }

    public <T> CompletableFuture<List<T>> getAll(Class<T> clazz) {
        if (!isConnected()) {
            logger.severe("Tried to perform database action when the database is not connected.");
            return CompletableFuture.completedFuture(List.of());
        }
        return CompletableFuture.supplyAsync(() -> handler.getAll(clazz), executor);
    }

    public <T> CompletableFuture<Optional<T>> get(Class<T> clazz, UUID id) {
        if (!isConnected()) {
            logger.severe("Tried to perform database action when the database is not connected.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return CompletableFuture.supplyAsync(() -> handler.get(clazz, id), executor);
    }


    public <T> CompletableFuture<Void> save(Class<T> clazz, T t) {
        if (!isConnected()) {
            logger.severe("Tried to perform database action when the database is not connected.");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> handler.save(clazz, t), executor);
    }

    public <T> CompletableFuture<Void> delete(Class<T> clazz, T t) {
        if (!isConnected()) {
            logger.severe("Tried to perform database action when the database is not connected.");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> handler.delete(clazz, t), executor);
    }

    public <T> CompletableFuture<Void> update(Class<T> clazz, T t, String[] params) {
        if (!isConnected()) {
            logger.severe("Tried to perform database action when the database is not connected.");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> handler.update(clazz, t, params), executor);
    }

    public <T> CompletableFuture<Void> fixSchemas() {
        if (!isConnected()) {
            logger.severe("Tried to perform database action when the database is not connected.");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> handler.fixSchemas(), executor);
    }

    public boolean isConnected() {
        return handler.isConnected();
    }

    @Close
    public void shutdown() {
        try {
            executor.shutdown();
            boolean success = executor.awaitTermination(10, TimeUnit.SECONDS);
            if (!success) throw new RuntimeException("Failed to shutdown thread pool");
            handler.destroy();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract Tuple<DatabaseType, Class<? extends DatabaseHandler>> getEnabledDatabase();

    private DatabaseHandler initHandler() {
        Tuple<DatabaseType, Class<? extends DatabaseHandler>> db = getEnabledDatabase();
        logger.info("DB Type: %s".formatted(db.first().getFriendlyName()));
        try {
            Class<? extends DatabaseHandler> handlerClass = db.second();
            if (handlerClass == null) {
                throw new IllegalStateException("No handler for database type %s registered!".formatted(db.first().getFriendlyName()));
            }
            return handlerClass.getDeclaredConstructor(BasePlugin.class).newInstance(plugin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
