/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.mcbe.manifest4j.gson;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.function.Predicate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;

// based on a code from javadoc of Gson,
// https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/TypeAdapterFactory.html
final class SnakeCaseEnum implements TypeAdapterFactory {

  private static final Pattern SNAKE_SEPARATOR = Pattern.compile("_+");
  
  @Override
  @SuppressWarnings("unchecked")
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    
    Class<T> rawType = (Class<T>) type.getRawType();
    if (!rawType.isEnum()) {
      return null;
    }

    final Map<T, String> constantToString = new HashMap<>();
    final Map<String, T> stringToConstant;

    Map<String, T> nameToConstant = new HashMap<>();
    Map<String, T> alternateToConstant = new HashMap<>();
    Set<String> duplicatedAlternates = new HashSet<>();
    for (T constant : rawType.getEnumConstants()) {
      final SerializedName annotation;
      try {
        annotation = constant.getClass()
            .getField(((Enum) constant).name())
            .getAnnotation(SerializedName.class);
      } catch (NoSuchFieldException e) {
        throw new UndeclaredThrowableException(e);
      }
      String str;
      if (annotation == null) {
        str = screamingSnakeToSnake(constant.toString());
      } else {
        str = Objects.requireNonNull(annotation.value(), "serialized name must not be null");
        for (String alternate : annotation.alternate()) {
          Objects.requireNonNull(alternate, "serialized name must not be null");
          if (alternateToConstant.containsKey(alternate)) {
            duplicatedAlternates.add(alternate);
          } else {
            alternateToConstant.put(alternate, constant);
          }
        }
      }
      constantToString.put(constant, str);
      if (nameToConstant.containsKey(str)) {
        throw new IllegalStateException("serialized names are duplicated : " + str);
      }
      nameToConstant.put(str, constant);
    }
    for (var s : duplicatedAlternates) {
      alternateToConstant.remove(s);
    }
    alternateToConstant.putAll(nameToConstant);
    stringToConstant = alternateToConstant;

    return new TypeAdapter<T>() {
      @Override
      public T read(JsonReader reader) throws IOException {
        return stringToConstant.get(reader.nextString());
      }

      @Override
      public void write(JsonWriter writer, T value) throws IOException {
        writer.value(constantToString.get(value));
      }
    }.nullSafe();
  }

  private String screamingSnakeToSnake(String str) {
    return SNAKE_SEPARATOR.splitAsStream(str)
        .filter(Predicate.not(String::isBlank))
        .map(s -> s.toLowerCase(Locale.ENGLISH))
        .collect(Collectors.joining("_"));
  }
}