/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.mcbe.manifest4j;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.UUID;

import io.github.risu729.mcbe.manifest4j.gson.ManifestGson;

public final class Module_ implements Comparable<Module_> {

  static final Set<EnumSet<Type>> PERMITTED_TYPE_SETS = Set.of(
      EnumSet.of(Type.RESOURCES),
      EnumSet.of(Type.DATA, Type.CLIENT_DATA, Type.INTERFACE, Type.SCRIPT),
      EnumSet.of(Type.WORLD_TEMPLATE),
      EnumSet.of(Type.SKIN_PACK));

  private static final Language DEFAULT_LANGUAGE = Language.JAVASCRIPT;

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

  public static Module_ of(Type type) {
    return switch (type) {
      case RESOURCES, DATA, WORLD_TEMPLATE, SKIN_PACK -> new Builder().type(type).build();
      default -> throw new UnsupportedOperationException("specified type is not supported : " + type);
    };
  }

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
    JAVASCRIPT("js");

    private final String extension;

    private Language(String extension) {
      this.extension = extension;
    }

    private String getExtension() {
      return extension;
    }
  }

  public static class Builder {

    private static final Pattern FILE_NAME_REGEX = Pattern.compile("^(?!^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(\\..*$|$))([^<>:\"/\\\\\\|\\?\\*\\x00-\\x20\\x7f][^<>:\"/\\\\\\|\\?\\*\\x00-\\x1f\\x7f]{0,253}[^\\.<>:\"/\\\\\\|\\?\\*\\x00-\\x20\\x7f]|[^\\.<>:\"/\\\\\\|\\?\\*\\x00-\\x20\\x7f])$");
    
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
      final var scripts = Path.of("scripts");
      for (int i = 0; i < 2; i++) {
        entry = switch (i) {
          case 0 -> entry.normalize();
          case 1 -> scripts.relativize(entry);
          default -> throw new AssertionError();
        };
        if (entry.getNameCount() == 1) {
          if (!FILE_NAME_REGEX.matcher(entry.toString()).matches()) {
            throw new IllegalArgumentException("invalid file name : " + entry);
          }
          this.entry = scripts.resolve(entry);
          return this;
        }
      }
      throw new IllegalArgumentException("entry must be a file directly under \"scripts\" : " + entry);
    }

    public Module_ build() {
      Objects.requireNonNull(type, "type is necessary");
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
        Objects.requireNonNull(entry, "entry is necessary when type is script");
        if (!FileSystems.getDefault()
            .getPathMatcher("glob:*." + language.getExtension())
            .matches(entry.getFileName())) {
          throw new IllegalStateException(
              "extension of entry must be " + language.getExtension()
                  + " when language is " + language + " : " + entry);
        }
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
    if (equals(other)) {
      return 0;
    }
    return type != other.type ? type.compareTo(other.type)
        : uuid.equals(other.uuid) ? uuid.compareTo(other.uuid)
            : version.compareTo(other.version);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return (obj instanceof Module_ other)
        && Objects.equals(uuid, other.uuid)
        && Objects.equals(version, other.version);
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 31 + Objects.hashCode(uuid);
    hash = hash * 31 + Objects.hashCode(version);
    return hash;
  }

  @Override
  public String toString() {
    return ManifestGson.SERIALIZE_NULLS.toJson(this);
  }
}