/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package risu729.mcbe.manifest4j;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

import risu729.mcbe.manifest4j.gson.ManifestGson;

public class Module_ implements Comparable<Module_> {

  static final Set<EnumSet<Type>> PERMITTED_TYPE_SETS = Set.of(
      EnumSet.of(Type.RESOURCES),
      EnumSet.of(Type.DATA, Type.CLIENT_DATA, Type.INTERFACE, Type.SCRIPT),
      EnumSet.of(Type.WORLD_TEMPLATE),
      EnumSet.of(Type.SKIN_PACK));

  private static final Language DEFAULT_LANGUAGE = Language.JAVASCRIPT;

  private static final NullsFirstComparator<Module_> COMPARATOR = NullsFirstComparator.comparing(Module_::getType)
      .thenComparing(Module_::getUUID)
      .thenComparing(Module_::getVersion)
      .thenComparing(Module_::getLanguage)
      .thenComparing(Module_::getEntry)
      .thenComparing(Module_::getDescription);

  // necessary
  // DATA, CLIENT_DATA, INTERFACE, and SCRIPT can be put in the same pack
  // other types can be put in the same pack only if the type is the same
  private final Type type;
  private final String description;
  private final UUID uuid; // necessary
  private final SemVer version; // necessary
  // necessary and only for when type == SCRIPT
  private final Language language;
  // necessary, only for, and must be a path of a file of the script when type ==
  // SCRIPT
  // the top directory must be "scripts" and the extension must be ".js"
  private final Path entry;

  public Type getType() {
    return type;
  }

  public String getDescription() {
    return description;
  }

  public UUID getUUID() {
    return uuid;
  }

  public SemVer getVersion() {
    return version;
  }

  public Language getLanguage() {
    return language;
  }

  public Path getEntry() {
    return entry;
  }

  public enum Type {
    RESOURCES, // resource pack
    DATA, // behavior pack
    CLIENT_DATA, // scripting API
    INTERFACE, // not well known, behavior pack?
    WORLD_TEMPLATE, // world template
    SCRIPT, // GameTest Framework
    SKIN_PACK // skin pack
    // TODO: add past types
  }

  public enum Language {
    @SerializedName("JavaScript")
    JAVASCRIPT
  }

  public static class Builder {
    private Type type;
    private String description;
    private UUID uuid;
    private SemVer version;
    private Language language;
    private Path entry;

    public Builder() {
    }

    public Builder(Module_ other) {
      type(other.type);
      description(other.description);
      uuid(other.uuid);
      version(other.version);
      language(other.language);
      entry(other.entry);
    }

    public Builder type(Type type) {
      this.type = Objects.requireNonNull(type, "type must not be null");
      return this;
    }

    public Builder description(String description) {
      if (description == null || description.isBlank()) {
        description = null;
      }
      this.description = description;
      return this;
    }

    public Builder uuid(UUID uuid) {
      this.uuid = uuid;
      return this;
    }

    public Builder version(SemVer version) {
      this.version = version;
      return this;
    }

    public Builder language(Language language) {
      this.language = language;
      return this;
    }

    public Builder entry(Path entry) {
      if (entry == null) {
        this.entry = null;
        return this;
      }
      entry = entry.normalize();
      if (entry.toString().isBlank()) {
        throw new IllegalArgumentException("entry must not be effectively empty : " + entry);
      }
      if (!FileSystems.getDefault()
          .getPathMatcher("glob:*.js")
          .matches(entry.getFileName())) {
        throw new IllegalArgumentException(
            "extension of entry must be .js : " + entry.toString());
      }
      final var scripts = Path.of("scripts");
      for (int i = 0; i < entry.getNameCount(); i++) {
        if (entry.getName(i).equals(scripts)) {
          this.entry = entry.subpath(i, entry.getNameCount());
          return this;
        }
      }
      if (entry.isAbsolute()) {
        throw new IllegalArgumentException(
            "entry must a file in \"scripts\" : " + entry.toString());
      }
      this.entry = scripts.resolve(entry);
      return this;
    }

    public Module_ build() {
      Objects.requireNonNull(type, "type must not be null");
      if (uuid == null) {
        uuid = UUID.randomUUID();
      }
      if (version == null) {
        version = SemVer.DEFAULT;
      }
      if (type == Type.SCRIPT) {
        if (language == null) {
          language = DEFAULT_LANGUAGE;
        }
        Objects.requireNonNull(entry, "entry must not be null when type is script");
      } else {
        if (language != null) {
          throw new IllegalStateException(
              "language must be null when type is not script");
        }
        if (entry != null) {
          throw new IllegalStateException(
              "entry must be null when type is not script");
        }
      }
      return new Module_(this);
    }
  }

  private Module_(Builder builder) {
    this.type = builder.type;
    this.description = builder.description;
    this.uuid = builder.uuid;
    this.version = builder.version;
    this.language = builder.language;
    this.entry = builder.entry;
  }

  @Override
  public int compareTo(Module_ other) {
    return COMPARATOR.compare(this, other);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return (obj instanceof Module_ other)
        && type == other.type
        && Objects.equals(description, other.description)
        && Objects.equals(uuid, other.uuid)
        && Objects.equals(version, other.version)
        && language == other.language
        && Objects.equals(entry, other.entry);
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 31 + Objects.hashCode(type);
    hash = hash * 31 + Objects.hashCode(description);
    hash = hash * 31 + Objects.hashCode(uuid);
    hash = hash * 31 + Objects.hashCode(version);
    hash = hash * 31 + Objects.hashCode(language);
    hash = hash * 31 + Objects.hashCode(entry);
    return hash;
  }

  @Override
  public String toString() {
    return ManifestGson.gsonSerializeNulls().toJson(this);
  }
}