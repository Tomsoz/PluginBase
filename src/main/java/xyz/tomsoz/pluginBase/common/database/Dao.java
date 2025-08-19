package xyz.tomsoz.pluginBase.common.database;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface representing a Data Access Object, a design pattern that provides an abstract interface to the database,
 * allowing for the separation of the database logic from the rest of the application.
 * @param <T> the type of the object that the DAO will be handling.
 */
public interface Dao<T> {
    /**
     * Get an object from the database by its id.
     *
     * @param id The id of the object to get
     * @return An optional containing the object if it exists, or an empty optional if it doesn't exist
     */
    Optional<T> get(UUID id);

    /**
     * Get all objects of type T from the database
     *
     * @return A list of all objects of type T in the database
     */
    List<T> getAll();

    /**
     * Save an object of type T to the database.
     *
     * @param t The object to save
     */
    void save(T t);

    /**
     * Update an object of type T in the database.
     *
     * @param t      The object to update
     * @param params The parameters to update this object with
     */
    void update(T t, String[] params);

    /**
     * Delete an object of type T from the database.
     *
     * @param t The object to delete
     */
    void delete(T t);
}
