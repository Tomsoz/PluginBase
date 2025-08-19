package xyz.tomsoz.pluginBase.common.database;

public interface SchemaUpgrader {
    void upgrade();

    boolean needsUpgrade();
}
