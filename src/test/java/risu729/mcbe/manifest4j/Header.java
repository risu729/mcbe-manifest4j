/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package risu729.mcbe.manifest4j;

import java.util.Objects;
import java.util.UUID;

import risu729.mcbe.manifest4j.gson.ManifestGson;

public class Header {
  
  static final Boolean DEFAULT_LOCK_TEMPLATE_OPTIONS = false;

  private final String name; // necessary
  private final String description;
  private final UUID uuid; // necessary
  private final SemVer version; // necessary
  // necessary, only for, and must be later than or equal to 1.13.0
  // when format_version is 2 and Module.type != WORLD_TEMPLATE or SKIN_PACK
  private final SemVer minEngineVersion;
  private final Boolean platformLocked;
  private final PackScope packScope;
  // necessary, only for, and must be later than or equal to 1.13.0
  // when Module.type == world_template
  private final SemVer baseGameVersion;
  // necessary and only for when Module.type == world_template
  private final Boolean lockTemplateOptions;

  public String getName() {
    return name;
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

  public SemVer getMinEngineVersion() {
    return minEngineVersion;
  }

  public Boolean getPlatformLocked() {
    return platformLocked;
  }

  public PackScope getPackScope() {
    return packScope;
  }

  public SemVer getBaseGameVersion() {
    return baseGameVersion;
  }

  public Boolean getLockTemplateOptions() {
    return lockTemplateOptions;
  }

  public enum PackScope {
    GLOBAL,
    WORLD
  }

  public static class Builder {
    private String name;
    private String description;
    private UUID uuid;
    private SemVer version;
    private SemVer minEngineVersion;
    private Boolean platformLocked;
    private PackScope packScope;
    private SemVer baseGameVersion;
    private Boolean lockTemplateOptions;

    public Builder() {
    }

    public Builder(Header other) {
      name(other.name);
      description(other.description);
      uuid(other.uuid);
      version(other.version);
      minEngineVersion(other.minEngineVersion);
      platformLocked(other.platformLocked);
      packScope(other.packScope);
      baseGameVersion(other.baseGameVersion);
      lockTemplateOptions(other.lockTemplateOptions);
    }

    public Builder name(String name) {
      if (name == null || name.isBlank()) {
        name = null;
      }
      this.name = Objects.requireNonNull(name, "name must not be null");
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

    public Builder minEngineVersion(SemVer minEngineVersion) {
      this.minEngineVersion = minEngineVersion;
      if (minEngineVersion != null
          && minEngineVersion.compareTo(new SemVer(1, 13, 0)) < 0) {
        throw new IllegalArgumentException(
            "min_engine_version must be later than or equal to 1.13.0 : " + minEngineVersion);
      }
      return this;
    }

    public Builder platformLocked(Boolean platformLocked) {
      this.platformLocked = platformLocked;
      return this;
    }

    public Builder packScope(PackScope packScope) {
      this.packScope = packScope;
      return this;
    }

    public Builder baseGameVersion(SemVer baseGameVersion) {
      this.baseGameVersion = baseGameVersion;
      if (baseGameVersion != null
          && baseGameVersion.compareTo(new SemVer(1, 13, 0)) < 0) {
        throw new IllegalStateException(
            "base_game_version must be later than or equal to 1.13.0 : "
                + baseGameVersion);
      }
      return this;
    }

    public Builder lockTemplateOptions(Boolean lockTemplateOptions) {
      this.lockTemplateOptions = lockTemplateOptions;
      return this;
    }

    public Header build() {
      Objects.requireNonNull(name, "name must not be null");
      if (uuid == null) {
        uuid = UUID.randomUUID();
      }
      if (version == null) {
        version = SemVer.DEFAULT;
      }
      return new Header(this);
    }
  }

  private Header(Builder builder) {
    this.name = builder.name;
    this.description = builder.description;
    this.uuid = builder.uuid;
    this.version = builder.version;
    this.minEngineVersion = builder.minEngineVersion;
    this.platformLocked = builder.platformLocked;
    this.packScope = builder.packScope;
    this.baseGameVersion = builder.baseGameVersion;
    this.lockTemplateOptions = builder.lockTemplateOptions;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    return (obj instanceof Header other)
        && Objects.equals(name, other.name)
        && Objects.equals(description, other.description)
        && Objects.equals(uuid, other.uuid)
        && Objects.equals(version, other.version)
        && Objects.equals(minEngineVersion, other.minEngineVersion)
        && Objects.equals(platformLocked, other.platformLocked)
        && packScope == other.packScope
        && Objects.equals(baseGameVersion, other.baseGameVersion)
        && Objects.equals(lockTemplateOptions, other.lockTemplateOptions);
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 31 + Objects.hashCode(name);
    hash = hash * 31 + Objects.hashCode(description);
    hash = hash * 31 + Objects.hashCode(uuid);
    hash = hash * 31 + Objects.hashCode(version);
    hash = hash * 31 + Objects.hashCode(minEngineVersion);
    hash = hash * 31 + Objects.hashCode(platformLocked);
    hash = hash * 31 + Objects.hashCode(packScope);
    hash = hash * 31 + Objects.hashCode(baseGameVersion);
    hash = hash * 31 + Objects.hashCode(lockTemplateOptions);
    return hash;
  }

  @Override
  public String toString() {
    return ManifestGson.gsonSerializeNulls().toJson(this);
  }
}