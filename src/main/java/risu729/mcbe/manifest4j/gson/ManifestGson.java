/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package risu729.mcbe.manifest4j.gson;

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import risu729.mcbe.manifest4j.Metadata;
import risu729.mcbe.manifest4j.SemVer;

public class ManifestGson {

  private static final GsonBuilder builder = new GsonBuilder()
      .setPrettyPrinting()
      .setFieldNamingStrategy(new SnakeCaseField())
      .registerTypeAdapterFactory(new SnakeCaseEnum())
      .registerTypeHierarchyAdapter(Path.class, new PathTypeAdapter().nullSafe())
      .registerTypeAdapter(SemVer.class, new SemVerTypeAdapter().nullSafe())
      .registerTypeAdapter(Metadata.GeneratedWith.class, new GeneratedWithTypeAdapter().nullSafe());

  private static final Gson gson = builder.create();
  private static final Gson gsonSerializeNulls = builder.serializeNulls().create();

  public static Gson gson() {
    return gson;
  }

  public static Gson gsonSerializeNulls() {
    return gsonSerializeNulls;
  }

  static String toSnakeCase(String str) {
    String regex = "_+|(?<=[A-Z])(?=[A-Z][a-z])|(?<=[^A-Z])(?=[A-Z])|(?<=[0-9])(?=[^A-Z0-9])|(?=[0-9])(?<=[^0-9])|(?<=[^A-Za-z0-9])(?=[a-z])|(?=[^A-Za-z0-9])(?<=[A-Za-z])";
    return Pattern.compile(regex)
        .splitAsStream(str)
        .filter(Predicate.not(String::isBlank))
        .map(s -> s.toLowerCase(Locale.ENGLISH))
        .collect(Collectors.joining("_"));
  }
}