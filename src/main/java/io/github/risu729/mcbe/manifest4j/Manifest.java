/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.mcbe.manifest4j;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.TreeSet;

import io.github.risu729.mcbe.manifest4j.gson.ManifestGson;

public final class Manifest {

  private static final Integer DEFAULT_FORMAT_VERSION = 2;
  private static final Integer MAX_FORMAT_VERSION = 2;

  private final Integer formatVersion; // necessary
  private final Header header; // necessary
  private final TreeSet<Module_> modules; // necesarry at least one
  private final TreeSet<Dependency> dependencies;
  private final EnumSet<Capability> capabilities;
  private final Metadata metadata;
  private final TreeSet<Subpack> subpacks;

  public static Manifest of(String name, Module_.Type type) {
    return new Builder().header(Header.of(name)).modules(Module_.of(type)).build();
  }

  public static Manifest fromJson(String json) {
    return ManifestGson.NORMAL.fromJson(json, Manifest.class);
  }

  public String toJson() {
    return ManifestGson.NORMAL.toJson(this);
  }
  
  public Integer getFormatVersion() {
    return formatVersion;
  }

  public Header getHeader() {
    return header;
  }

  @SuppressWarnings("unchecked")
  public TreeSet<Module_> getModules() {
    return (TreeSet<Module_>) modules.clone();
  }

  @SuppressWarnings("unchecked")
  public TreeSet<Dependency> getDependencies() {
    return (TreeSet<Dependency>) dependencies.clone();
  }

  public EnumSet<Capability> getCapabilities() {
    return capabilities.clone();
  }

  public Metadata getMetadata() {
    return metadata;
  }

  @SuppressWarnings("unchecked")
  public TreeSet<Subpack> getSubpacks() {
    return (TreeSet<Subpack>) subpacks.clone();
  }

  public static class Builder {

    private Integer formatVersion;
    private Header header;
    private TreeSet<Module_> modules;
    private TreeSet<Dependency> dependencies;
    private EnumSet<Capability> capabilities;
    private Metadata metadata;
    private TreeSet<Subpack> subpacks;

    public Builder() {
    }

    public Builder(Manifest other) {
      formatVersion(other.formatVersion);
      header(other.header);
      modules(other.modules);
      dependencies(other.dependencies);
      capabilities(other.capabilities);
      metadata(other.metadata);
      subpacks(other.subpacks);
    }

    public Builder formatVersion(Integer formatVersion) {
      if (formatVersion != null
          && (formatVersion < 1 || formatVersion > MAX_FORMAT_VERSION)) {
        throw new IllegalArgumentException(
            "format_version must be a positive integer which is" + MAX_FORMAT_VERSION
                + "or less : " + formatVersion);
      }
      this.formatVersion = formatVersion;
      return this;
    }

    public Builder header(Header header) {
      this.header = Objects.requireNonNull(header, "header must not be null");
      return this;
    }

    public Builder modules(Module_... modules) {
      return modules(modules == null ? null : Set.of(modules));
    }

    public Builder modules(Collection<Module_> modules) {
      this.modules = null;
      return addModules(modules);
    }

    public Builder addModules(Module_... modules) {
      return addModules(modules == null ? null : Set.of(modules));
    }

    public Builder addModules(Collection<Module_> modules) {
      if (modules == null || modules.isEmpty()) {
        return this;
      }
      for (var e : modules) {
        Objects.requireNonNull(e, "module must not be null");
      }
      if (this.modules == null) {
        this.modules = new TreeSet<>(modules);
        return this;
      }
      this.modules.addAll(modules);
      return this;
    }

    public Builder dependencies(Dependency... dependencies) {
      return dependencies(dependencies == null ? null : Set.of(dependencies));
    }

    public Builder dependencies(Collection<Dependency> dependencies) {
      this.dependencies = null;
      return addDependencies(dependencies);
    }

    public Builder addDependencies(Dependency... dependencies) {
      return addDependencies(dependencies == null ? null : Set.of(dependencies));
    }

    public Builder addDependencies(Collection<Dependency> dependencies) {
      if (dependencies == null || dependencies.isEmpty()) {
        return this;
      }
      for (var e : dependencies) {
        Objects.requireNonNull(e, "dependency must not be null");
      }
      if (this.dependencies == null) {
        this.dependencies = new TreeSet<>(dependencies);
        return this;
      }
      this.dependencies.addAll(dependencies);
      return this;
    }

    public Builder capabilities(Capability... capabilities) {
      return capabilities(capabilities == null ? null : Set.of(capabilities));
    }

    public Builder capabilities(Collection<Capability> capabilities) {
      this.capabilities = null;
      return addCapabilities(capabilities);
    }

    public Builder addCapabilities(Capability... capabilities) {
      return addCapabilities(capabilities == null ? null : Set.of(capabilities));
    }

    public Builder addCapabilities(Collection<Capability> capabilities) {      
      if (capabilities == null || capabilities.isEmpty()) {
        return this;
      }
      for (var e : capabilities) {
        Objects.requireNonNull(e, "capability must not be null");
      }
      if (this.capabilities == null) {
        this.capabilities = EnumSet.copyOf(capabilities);
        return this;
      }
      this.capabilities.addAll(capabilities);
      return this;
    }

    public Builder metadata(Metadata metadata) {
      this.metadata = metadata;
      return this;
    }

    public Builder subpacks(Subpack... subpacks) {
      return subpacks(subpacks == null ? null : Set.of(subpacks));
    }

    public Builder subpacks(Collection<Subpack> subpacks) {
      this.subpacks = null;
      return addSubpacks(subpacks);
    }

    public Builder addSubpacks(Subpack... subpacks) {
      return addSubpacks(subpacks == null ? null : Set.of(subpacks));
    }

    public Builder addSubpacks(Collection<Subpack> subpacks) {
      if (subpacks == null || subpacks.isEmpty()) {
        return this;
      }
      for (var e : subpacks) {
        Objects.requireNonNull(e, "subpack must not be null");
      }
      if (this.subpacks == null) {
        this.subpacks = new TreeSet<>(subpacks);
        return this;
      }
      this.subpacks.addAll(subpacks);
      return this;
    }

    public Manifest build() {
      Objects.requireNonNull(header, "header is necessary");
      Objects.requireNonNull(modules, "modules are necessary at least one");
      
      if (formatVersion == null) {
        formatVersion = DEFAULT_FORMAT_VERSION;
      }

      EnumSet<Module_.Type> types = modules.stream()
          .map(Module_::getType)
          .collect(Collectors.toCollection(() -> EnumSet.noneOf(Module_.Type.class)));
      for (EnumSet<Module_.Type> set : Module_.PERMITTED_TYPE_SETS) {
        for (var type : set) {
          if (types.contains(type)) {
            for (var e : EnumSet.complementOf(set)) {
              if (types.contains(e)) {
                throw new IllegalStateException(
                    "modules must not contain both types of : " + type + " and " + e);
              }
            }
            break;
          }
        }
      }

      if (formatVersion == 2
          && !(types.contains(Module_.Type.WORLD_TEMPLATE)
              || types.contains(Module_.Type.SKIN_PACK))) {
        if (header.getMinEngineVersion() == null) {
          header(new Header.Builder(header)
              .minEngineVersion(SemVer.MCBE_DEFAULT)
              .build());
        }
      } else {
        if (header.getMinEngineVersion() != null) {
          throw new IllegalStateException(
              "min_engine_version must be null if the format_version is 1 or the type of module is skin_pack or world_template : "
                  + header.getMinEngineVersion());
        }
      }

      if (types.contains(Module_.Type.WORLD_TEMPLATE)) {
        if (header.getBaseGameVersion() == null) {
          header(new Header.Builder(header)
              .baseGameVersion(SemVer.MCBE_DEFAULT)
              .build());
        }
        if (header.getLockTemplateOptions() == null) {
          header(new Header.Builder(header)
              .lockTemplateOptions(Header.DEFAULT_LOCK_TEMPLATE_OPTIONS)
              .build());
        }
      } else {
        if (header.getBaseGameVersion() != null) {
          throw new IllegalStateException(
              "base_game_version must be null if the type of module is not world_template : "
                  + header.getBaseGameVersion());
        }
        if (header.getLockTemplateOptions() != null) {
          throw new IllegalStateException(
              "lock_template_options must be null if the type of modules is not world_template : "
                  + header.getLockTemplateOptions());
        }
      }

      return new Manifest(this);
    }
  }

  private Manifest(Builder builder) {
    this.formatVersion = builder.formatVersion;
    this.header = builder.header;
    this.modules = builder.modules;
    this.dependencies = builder.dependencies;
    this.capabilities = builder.capabilities;
    this.metadata = builder.metadata;
    this.subpacks = builder.subpacks;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return (obj instanceof Manifest other)
        && Objects.equals(header, other.header);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(header);
  }

  @Override
  public String toString() {
    return ManifestGson.SERIALIZE_NULLS.toJson(this);
  }
}