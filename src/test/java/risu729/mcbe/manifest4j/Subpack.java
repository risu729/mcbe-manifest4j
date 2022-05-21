/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package risu729.mcbe.manifest4j;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Objects;

import risu729.mcbe.manifest4j.gson.ManifestGson;

public class Subpack implements Comparable<Subpack> {

  private static final NullsFirstComparator<Subpack> COMPARATOR = NullsFirstComparator.comparing(Subpack::getName)
      .thenComparing(Subpack::getFolderName)
      .thenComparing(Subpack::getMemoryTier);

  private final Path folderName; // necessary
  private final String name; // necessary
  private final Integer memoryTier; // 1 memory_tier == 0.25GB

  public Path getFolderName() {
    return folderName;
  }

  public String getName() {
    return name;
  }

  public Integer getMemoryTier() {
    return memoryTier;
  }

  public static class Builder {
    private Path folderName;
    private String name;
    private Integer memoryTier;

    public Builder() {
    }

    public Builder(final Subpack other) {
      folderName(other.folderName);
      name(other.name);
      memoryTier(other.memoryTier);
    }

    public Builder folderName(Path folderName) {
      if (folderName == null) {
        this.folderName = null;
        return this;
      }
      folderName = folderName.normalize();
      if (folderName.toString().isBlank()) {
        throw new IllegalArgumentException("folder_name must not be effectively empty : " + folderName);
      }
      if (FileSystems.getDefault()
          .getPathMatcher("glob:*")
          .matches(folderName)) {
        this.folderName = folderName;
        return this;
      }

      final var subpacks = Path.of("subpacks");
      final int count = folderName.getNameCount();
      if (folderName.subpath(count - 2, count - 1).equals(subpacks)) {
        this.folderName = folderName.getFileName();
        return this;
      }
      throw new IllegalArgumentException(
          "folder_name must be a directory in \"subpacks\" : " + folderName);
    }

    public Builder name(String name) {
      if (name == null || name.isBlank()) {
        name = null;
      }
      this.name = name;
      return this;
    }

    public Builder memoryTier(Integer memoryTier) {
      this.memoryTier = memoryTier;
      if (memoryTier != null
          && memoryTier < 0) {
        throw new IllegalArgumentException(
            "memory_tier must not be negative : " + memoryTier);
      }
      return this;
    }

    public Subpack build() {
      Objects.requireNonNull(folderName, "folder_name must not be null");
      if (name == null) {
        name = folderName.toString();
      }
      return new Subpack(this);
    }
  }

  private Subpack(Builder builder) {
    this.folderName = builder.folderName;
    this.name = builder.name;
    this.memoryTier = builder.memoryTier;
  }

  @Override
  public int compareTo(Subpack other) {
    return COMPARATOR.compare(this, other);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return (obj instanceof Subpack other)
        && Objects.equals(folderName, other.folderName)
        && Objects.equals(name, other.name)
        && Objects.equals(memoryTier, other.memoryTier);
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 31 + Objects.hashCode(folderName);
    hash = hash * 31 + Objects.hashCode(name);
    hash = hash * 31 + Objects.hashCode(memoryTier);
    return hash;
  }

  @Override
  public String toString() {
    return ManifestGson.gsonSerializeNulls().toJson(this);
  }
}