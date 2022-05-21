/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package risu729.mcbe.manifest4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Function;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.TreeSet;

import risu729.mcbe.manifest4j.gson.ManifestGson;

public class Manifest {

  private static final Integer DEFAULT_FORMAT_VERSION = 2;
  private static final Integer MAX_FORMAT_VERSION = 2;

  private final Integer formatVersion; // necessary
  private final Header header; // necessary
  private final SortedSet<Module_> modules; // necesarry at least one
  private final SortedSet<Dependency> dependencies;
  private final EnumSet<Capability> capabilities;
  private final Metadata metadata;
  private final SortedSet<Subpack> subpacks;

  public Integer getFormatVersion() {
    return formatVersion;
  }

  public Header getHeader() {
    return header;
  }

  public SortedSet<Module_> getModules() {
    return modules;
  }

  public SortedSet<Dependency> getDependencies() {
    return dependencies;
  }

  public EnumSet<Capability> getCapabilities() {
    return capabilities;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public SortedSet<Subpack> getSubpacks() {
    return subpacks;
  }

  public String toJson() {
    return ManifestGson.gson().toJson(this);
  }

  public static Manifest fromJson(String json) {
    return ManifestGson.gson().fromJson(json, Manifest.class);
  }

  public static class Builder {
    private Integer formatVersion;
    private Header header;
    private SortedSet<Module_> modules;
    private SortedSet<Dependency> dependencies;
    private EnumSet<Capability> capabilities;
    private Metadata metadata;
    private SortedSet<Subpack> subpacks;

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
      if (formatVersion == null
          || (formatVersion >= 1 && formatVersion <= MAX_FORMAT_VERSION)) {
        this.formatVersion = formatVersion;
        return this;
      }
      throw new IllegalArgumentException(
          "format_version must be a positive integer which is" + MAX_FORMAT_VERSION
              + "or less : " + formatVersion);
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
      this.modules = addCollection(modules, this.modules);
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
      this.dependencies = addCollection(dependencies, this.dependencies);
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
      EnumSet<Capability> capabilitiesSet = null;
      if (capabilities != null) {
        capabilitiesSet = capabilities.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(
                () -> EnumSet.noneOf(Capability.class)));
        if (capabilitiesSet.isEmpty()) {
          capabilitiesSet = null;
        }
      }
      if (this.capabilities == null) {
        this.capabilities = capabilitiesSet;
        return this;
      }
      if (capabilitiesSet != null) {
        this.capabilities.addAll(capabilitiesSet);
      }
      return this;
    }

    public Builder metadata(Metadata metadata) {
      if (metadata == null || metadata.isEmpty()) {
        metadata = null;
      }
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
      this.subpacks = addCollection(subpacks, this.subpacks);
      return this;
    }

    public Manifest build() {
      if (formatVersion == null) {
        formatVersion = DEFAULT_FORMAT_VERSION;
      }
      Objects.requireNonNull(header, "header must not be null");
      Objects.requireNonNull(modules, "modules must not be null");

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
              "min_engine_version must be null if the fomart_version is 1 or the type of module is skin_pack or world_template : "
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

    private static <C> SortedSet<C> addCollection(Collection<C> addend, SortedSet<C> augend) {
      SortedSet<C> set = null;
      if (addend != null) {
        set = addend.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(TreeSet::new));
        if (set.isEmpty()) {
          set = null;
        }
      }
      if (augend == null) {
        augend = set;
        return augend;
      }
      if (set != null) {
        augend.addAll(set);
        return augend;
      }
      return augend;
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
        && Objects.equals(formatVersion, other.formatVersion)
        && Objects.equals(header, other.header)
        && Objects.equals(modules, other.modules)
        && Objects.equals(dependencies, other.dependencies)
        && Objects.equals(capabilities, other.capabilities)
        && Objects.equals(metadata, other.metadata)
        && Objects.equals(subpacks, other.subpacks);
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 31 + Objects.hashCode(formatVersion);
    hash = hash * 31 + Objects.hashCode(header);
    hash = hash * 31 + Objects.hashCode(modules);
    hash = hash * 31 + Objects.hashCode(dependencies);
    hash = hash * 31 + Objects.hashCode(capabilities);
    hash = hash * 31 + Objects.hashCode(metadata);
    hash = hash * 31 + Objects.hashCode(subpacks);
    return hash;
  }

  @Override
  public String toString() {
    return ManifestGson.gsonSerializeNulls().toJson(this);
  }
}