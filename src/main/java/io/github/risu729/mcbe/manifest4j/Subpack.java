/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.mcbe.manifest4j;

import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Pattern;

import io.github.risu729.mcbe.manifest4j.gson.ManifestGson;

public final class Subpack implements Comparable<Subpack> {

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

    private static final Pattern FOLDER_NAME_REGEX = Pattern.compile(
        "^(?!^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$)([^\\.<>:\"/\\\\\\|\\?\\*\\x00-\\x20\\x7f][^\\.<>:\"/\\\\\\|\\?\\*\\x00-\\x1f\\x7f]{0,253}[^\\.<>:\"/\\\\\\|\\?\\*\\x00-\\x20\\x7f]|[^\\.<>:\"/\\\\\\|\\?\\*\\x00-\\x20\\x7f])$",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

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
      Objects.requireNonNull(folderName, "folder_name must not be null");
      for (int i = 0; i < 2; i++) {
        folderName = switch (i) {
          case 0 -> folderName.normalize();
          case 1 -> Path.of("subpacks").relativize(folderName);
          default -> throw new AssertionError();
        };
        if (folderName.getNameCount() == 1) {
          if (!FOLDER_NAME_REGEX.matcher(folderName.toString()).matches()) {
            throw new IllegalArgumentException("invalid folder name : " + folderName);
          }
          this.folderName = folderName;
          return this;
        }
      }
      throw new IllegalArgumentException(
          "folder_name must be a directory directly under \"subpacks\" : " + folderName);
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder memoryTier(Integer memoryTier) {
      if (memoryTier != null && memoryTier < 0) {
        throw new IllegalArgumentException(
            "memory_tier must not be negative : " + memoryTier);
      }
      this.memoryTier = memoryTier;
      return this;
    }

    public Subpack build() {
      Objects.requireNonNull(folderName, "folder_name is necessary");
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
    if (equals(other)) {
      return 0;
    }
    return !name.equals(other.name) ? name.compareTo(other.name)
        : folderName.compareTo(other.folderName);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return (obj instanceof Subpack other)
        && Objects.equals(folderName, other.folderName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(folderName);
  }

  @Override
  public String toString() {
    return ManifestGson.SERIALIZE_NULLS.toJson(this);
  }
}