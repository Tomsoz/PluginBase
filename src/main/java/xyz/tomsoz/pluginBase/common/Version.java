package xyz.tomsoz.pluginBase.common;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A utility for parsing and them comparing a semantic version string
 * <p>
 *
 * @author Preva1l
 * @author Tomsoz
 */
@SuppressWarnings("unused")
public class Version implements Comparable<Version> {
    public final String VERSION_DELIMITER = ".";
    public static final String META_DELIMITER = "-";

    // Major, minor and patch version numbers
    private int[] versions = new int[]{};
    @NotNull
    private String metadata = "";
    @NotNull
    private String metaSeparator = "";

    protected Version() {
    }

    private Version(@NotNull String version, @NotNull String metaDelimiter) {
        this.parse(version, metaDelimiter);
        this.metaSeparator = metaDelimiter;
    }

    /**
     * Create a new {@link Version} by parsing a string
     *
     * @param version       The version string to parse
     * @param metaDelimiter The delimiter separating version numbers from metadata to use
     * @return The {@link Version}
     */
    @NotNull
    public static Version fromString(@NotNull String version, @NotNull String metaDelimiter) {
        return new Version(version, metaDelimiter);
    }

    /**
     * Create a new {@link Version} by parsing a string
     *
     * @param versionString The version string to parse
     * @return The {@link Version}
     * @implNote The default meta delimiter that will be used is {@link #META_DELIMITER}
     */
    @NotNull
    public static Version fromString(@NotNull String versionString) {
        return new Version(versionString, META_DELIMITER);
    }

    /**
     * Create a new {@link Version} by passing in a major, minor and patch
     *
     * @param major The major version number
     * @param minor The minor version number
     * @param patch The patch version number
     * @return The {@link Version}
     */
    @NotNull
    public static Version of(int major, int minor, int patch) {
        return fromString(major + "." + minor + "." + patch);
    }

    /**
     * Parses a version string, including metadata, with the specified delimiter
     *
     * @param version       The version string to parse
     * @param metaDelimiter The metadata delimiter
     */
    private void parse(@NotNull String version, @NotNull String metaDelimiter) {
        int metaIndex = version.indexOf(metaDelimiter);
        if (metaIndex > 0) {
            this.metadata = version.substring(metaIndex + 1);
            version = version.substring(0, metaIndex);
        }

        String[] versions = version.split(Pattern.quote(VERSION_DELIMITER));
        try {
            this.versions = Arrays.stream(versions).mapToInt(Integer::parseInt).toArray();
        } catch (NumberFormatException e) {
            Logger.getLogger(this.getClass().getName()).severe("Invalid Version \"%s\" Using \"0.0.0.\"".formatted(version));
            this.versions = new int[]{0, 0, 0};
        }
    }

    /**
     * Compare this {@link Version} to another {@link Version}
     *
     * @param other The object to be compared
     * @return A negative integer, zero, or a positive integer as this version is less than, equal to, or greater
     * than the other version in terms of the semantic major, minor and patch versioning standard.
     */
    @Override
    public int compareTo(@NotNull Version other) {
        int length = Math.max(this.versions.length, other.versions.length);
        for (int i = 0; i < length; i++) {
            int a = i < this.versions.length ? this.versions[i] : 0;
            int b = i < other.versions.length ? other.versions[i] : 0;

            if (a < b) return -1;
            if (a > b) return 1;
        }

        return 0;
    }

    /**
     * Returns true if this {@link Version} is greater than {@code min}, but less than {@code max}
     *
     * @param min The minimum version
     * @param max The maximum version
     * @return Whether this {@link Version} is between {@code min} and {@code max}
     */
    public boolean isBetween(Version min, Version max) {
        return this.compareTo(min) >= 0 && this.compareTo(max) <= 0;
    }

    /**
     * Get the string representation of this {@link Version}
     *
     * @return The string representation of this {@link Version}
     */
    @Override
    @NotNull
    public String toString() {
        final StringJoiner joiner = new StringJoiner(VERSION_DELIMITER);
        for (int version : this.versions) {
            joiner.add(String.valueOf(version));
        }
        return joiner + ((!this.metadata.isEmpty()) ? (this.metaSeparator + this.metadata) : "");
    }

    /**
     * Get the string representation of this {@link Version}, without metadata
     *
     * @return The string representation of this {@link Version}, without metadata
     */
    @NotNull
    public String toStringWithoutMetadata() {
        final StringJoiner joiner = new StringJoiner(VERSION_DELIMITER);
        for (int version : this.versions) {
            joiner.add(String.valueOf(version));
        }
        return joiner.toString();
    }

    /**
     * Returns true if this {@link Version} is a pre-release, false otherwise
     *
     * @return Whether this is a pre-release
     */
    public boolean isPreRelease() {
        return metadata.matches("(?i)(alpha|beta|rc).*");
    }

    /**
     * Get the major version number.
     *
     * @return The major version number.
     */
    public int getMajor() {
        return versions.length > 0 ? versions[0] : 0;
    }

    /**
     * Get the minor version number.
     *
     * @return The minor version number.
     */
    public int getMinor() {
        return versions.length > 1 ? versions[1] : 0;
    }

    /**
     * Get the patch version number.
     *
     * @return The patch version number.
     */
    public int getPatch() {
        return versions.length > 2 ? versions[2] : 0;
    }

    /**
     * Get the version metadata
     *
     * @return The metadata
     */
    @NotNull
    public String getMetadata() {
        return this.metadata;
    }
}
