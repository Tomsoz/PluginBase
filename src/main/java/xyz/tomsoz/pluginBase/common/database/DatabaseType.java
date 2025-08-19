package xyz.tomsoz.pluginBase.common.database;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public enum DatabaseType {
    MONGO("mongodb", "MongoDB"),
    MYSQL("mysql", "MySQL"),
    MARIADB("mariadb", "MariaDB"),
    SQLITE("sqlite", "SQLite"),
    POSTGRES("postgres", "PostgreSQL");

    private final String id;
    private final String friendlyName;
}
