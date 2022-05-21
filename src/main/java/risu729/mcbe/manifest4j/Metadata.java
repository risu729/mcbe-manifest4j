/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package risu729.mcbe.manifest4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.TreeSet;

import risu729.mcbe.manifest4j.gson.ManifestGson;

public class Metadata {

  private final SortedSet<String> authors;
  private final URL url;
  private final String license;
  private final GeneratedWith generatedWith;

  public SortedSet<String> getAuthors() {
    return authors;
  }

  public URL getURL() {
    return url;
  }

  public String getLicense() {
    return license;
  }

  public GeneratedWith getGeneratedWith() {
    return generatedWith;
  }

  public boolean isEmpty() {
    return authors == null
        && url == null
        && license == null
        && generatedWith == null;
  }

  public static class GeneratedWith {
    private static final SortedSet<SemVer> DEFAULT_VERSIONS = new TreeSet<>(Set.of(SemVer.DEFAULT));
    // necessary, must be 32 characters maxmum
    // must contain only alphabets, numbers, underscores, and hyphens
    private final String name;
    private final SortedSet<SemVer> versions; // necessary at least one

    public String getName() {
      return name;
    }

    public SortedSet<SemVer> getVersions() {
      return versions;
    }

    public static class Builder {
      private String name;
      private SortedSet<SemVer> versions;

      public Builder() {
      }

      public Builder(GeneratedWith other) {
        name(other.name);
        versions(other.versions);
      }

      public Builder(Metadata metadata) {
        this(metadata.getGeneratedWith());
      }

      public Builder name(String name) {
        if (name == null || name.isBlank()) {
          name = null;
        }
        this.name = Objects.requireNonNull(name, "name must not be null");
        if (name.length() > 32 || !name.matches("^[\\w-]+$")) {
          throw new IllegalArgumentException(
              "name must be 32 characters maximum and must contain only alphabets, numbers, underscores, and hyphens : "
                  + name);
        }
        return this;
      }

      public Builder versions(SemVer... versions) {
        return versions(versions == null ? null : Set.of(versions));
      }

      public Builder versions(Collection<SemVer> versions) {
        this.versions = null;
        return addVersions(versions);
      }

      public Builder addVersions(SemVer... versions) {
        return addVersions(versions == null ? null : Set.of(versions));
      }

      public Builder addVersions(Collection<SemVer> versions) {
        SortedSet<SemVer> versionsSet = null;
        if (versions != null) {
          versionsSet = versions.stream()
              .filter(Objects::nonNull)
              .collect(Collectors.toCollection(TreeSet::new));
          if (versionsSet.isEmpty()) {
            versionsSet = null;
          }
        }
        if (this.versions == null) {
          this.versions = versionsSet;
          return this;
        }
        if (versionsSet != null) {
          this.versions.addAll(versionsSet);
        }
        return this;
      }

      public GeneratedWith build() {
        Objects.requireNonNull(name, "name must not be null");
        if (versions == null) {
          versions = DEFAULT_VERSIONS;
        }
        return new GeneratedWith(this);
      }
    }

    private GeneratedWith(Builder builder) {
      this.name = builder.name;
      this.versions = builder.versions;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      return (obj instanceof GeneratedWith other)
          && Objects.equals(name, other.name)
          && Objects.equals(versions, other.versions);
    }

    @Override
    public int hashCode() {
      int hash = 1;
      hash = hash * 31 + Objects.hashCode(name);
      hash = hash * 31 + Objects.hashCode(versions);
      return hash;
    }

    @Override
    public String toString() {
      return ManifestGson.gsonSerializeNulls().toJson(this);
    }
  }

  public static class Builder {
    private SortedSet<String> authors;
    private URL url;
    private String license;
    private GeneratedWith generatedWith;

    public Builder() {
    }

    public Builder(final Metadata metadata) {
      authors(metadata.authors);
      url(metadata.url);
      license(metadata.license);
      generatedWith(metadata.generatedWith);
    }

    public Builder authors(String... authors) {
      return authors(authors == null ? null : Set.of(authors));
    }

    public Builder authors(Collection<String> authors) {
      this.authors = null;
      return addAuthors(authors);
    }

    public Builder addAuthors(String... authors) {
      return addAuthors(authors == null ? null : Set.of(authors));
    }

    public Builder addAuthors(Collection<String> authors) {
      SortedSet<String> authorsSet = null;
      if (authors != null) {
        authorsSet = authors.stream()
            .filter(Objects::nonNull)
            .filter(Predicate.not(String::isBlank))
            .collect(Collectors.toCollection(TreeSet::new));
        if (authorsSet.isEmpty()) {
          authorsSet = null;
        }
      }
      if (this.authors == null) {
        this.authors = authorsSet;
        return this;
      }
      if (authorsSet != null) {
        this.authors.addAll(authorsSet);
      }
      return this;
    }

    public Builder url(URL url) {
      this.url = url;
      return this;
    }

    public Builder url(String url) {
      try {
        return url(url == null ? null : new URL(url));
      } catch (MalformedURLException e) {
        throw new IllegalArgumentException(e);
      }
    }

    public Builder license(String license) {
      if (license == null || license.isBlank()) {
        license = null;
      }
      this.license = license;
      return this;
    }

    public Builder generatedWith(GeneratedWith generatedWith) {
      this.generatedWith = generatedWith;
      return this;
    }

    public Metadata build() {
      return new Metadata(this);
    }
  }

  private Metadata(Builder builder) {
    this.authors = builder.authors;
    this.url = builder.url;
    this.license = builder.license;
    this.generatedWith = builder.generatedWith;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return (obj instanceof Metadata other)
        && Objects.equals(authors, other.authors)
        && Objects.equals(url, other.url)
        && Objects.equals(license, other.license)
        && Objects.equals(generatedWith, other.generatedWith);
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 31 + Objects.hashCode(authors);
    hash = hash * 31 + Objects.hashCode(url);
    hash = hash * 31 + Objects.hashCode(license);
    hash = hash * 31 + Objects.hashCode(generatedWith);
    return hash;
  }

  @Override
  public String toString() {
    return ManifestGson.gsonSerializeNulls().toJson(this);
  }
}