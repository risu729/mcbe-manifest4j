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

public class Dependency implements Comparable<Dependency> {

  private static final NullsFirstComparator<Dependency> COMPARATOR = NullsFirstComparator.comparing(Dependency::getUUID)
      .thenComparing(Dependency::getVersion);

  private final UUID uuid; // necessary
  private final SemVer version; // necessary

  public UUID getUUID() {
    return uuid;
  }

  public SemVer getVersion() {
    return version;
  }

  public static class Builder {
    private UUID uuid;
    private SemVer version;

    public Builder() {
    }

    public Builder(Dependency other) {
      uuid(other.uuid);
      version(other.version);
    }

    public Builder(Manifest manifest) {
      this(manifest.getHeader());
    }

    public Builder(Header header) {
      uuid(header.getUUID());
      version(header.getVersion());
    }

    public Builder uuid(UUID uuid) {
      this.uuid = Objects.requireNonNull(uuid, "The UUID must not be null.");
      return this;
    }

    public Builder version(SemVer version) {
      this.version = version;
      return this;
    }

    public Dependency build() {
      Objects.requireNonNull(uuid, "The UUID must not be null.");
      if (version == null) {
        version = SemVer.DEFAULT;
      }
      return new Dependency(this);
    }
  }

  private Dependency(Builder builder) {
    this.uuid = builder.uuid;
    this.version = builder.version;
  }

  @Override
  public int compareTo(Dependency other) {
    return COMPARATOR.compare(this, other);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    return (obj instanceof Dependency other)
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
    return ManifestGson.gsonSerializeNulls().toJson(this);
  }
}
